package com.b1.infrastructure.seed;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.b1.module.file.entity.FileStorage;

import com.b1.module.file.mapper.FileStorageMapper;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.minio.BucketExistsArgs;

import io.minio.ListObjectsArgs;

import io.minio.MakeBucketArgs;

import io.minio.MinioClient;

import io.minio.PutObjectArgs;

import io.minio.RemoveObjectArgs;

import io.minio.Result;

import io.minio.messages.Item;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.boot.context.event.ApplicationReadyEvent;

import org.springframework.context.event.EventListener;

import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;



import java.io.InputStream;

import java.nio.file.Files;

import java.nio.file.Path;

import java.time.LocalDateTime;
import java.util.Set;



/**

 * 开发环境：将 Material/ 目录中的演示素材同步至 MinIO，并写入 file_storage 元数据。

 * 启动时会清理旧的 material/seed/ 数据后全量重种，避免 object_key 冲突。

 */

@Slf4j

@Component

@Order(200)

@RequiredArgsConstructor

@ConditionalOnProperty(name = "b1.material.seed-enabled", havingValue = "true", matchIfMissing = false)

public class MaterialSeedRunner {



    private static final String SEED_PREFIX = "material/seed/";

    private static final Set<String> SEED_BUCKETS = Set.of("submissions", "reports", "knowledge");



    private final MinioClient minioClient;

    private final FileStorageMapper fileStorageMapper;

    private final ObjectMapper objectMapper;



    @Value("${b1.material.path:../Material}")

    private String materialPath;



    @Value("${b1.material.clean-before-seed:true}")

    private boolean cleanBeforeSeed;



    @EventListener(ApplicationReadyEvent.class)

    public void syncMaterialFiles() {

        Path root = Path.of(materialPath).toAbsolutePath().normalize();

        Path manifest = root.resolve("manifest.json");

        if (!Files.isRegularFile(manifest)) {

            log.warn("Material manifest not found: {} — skip MinIO seed", manifest);

            return;

        }



        try (InputStream in = Files.newInputStream(manifest)) {

            JsonNode rootNode = objectMapper.readTree(in);

            JsonNode files = rootNode.get("files");

            if (files == null || !files.isArray()) {

                log.warn("Material manifest has no files array");

                return;

            }



            if (cleanBeforeSeed) {
                cleanSeedData();
            }



            int uploaded = 0;

            for (JsonNode entry : files) {

                if (syncOne(root, entry)) {

                    uploaded++;

                }

            }

            log.info("Material seed complete: {} file(s) synced from {}", uploaded, root);

        } catch (Exception e) {

            log.error("Material seed failed", e);

        }

    }



    private void cleanSeedData() throws Exception {

        log.info("Cleaning previous material seed data...");



        for (String bucket : SEED_BUCKETS) {

            purgeMinioSeedPrefix(bucket);

        }



        fileStorageMapper.delete(new LambdaQueryWrapper<FileStorage>()
                .likeRight(FileStorage::getObjectKey, SEED_PREFIX));

        log.info("Material seed cleanup done (MinIO prefix {} + DB records)", SEED_PREFIX);

    }



    private void purgeMinioSeedPrefix(String bucket) throws Exception {

        ensureBucket(bucket);

        Iterable<Result<Item>> results = minioClient.listObjects(

                ListObjectsArgs.builder().bucket(bucket).prefix(SEED_PREFIX).recursive(true).build());

        for (Result<Item> result : results) {

            String objectName = result.get().objectName();

            minioClient.removeObject(RemoveObjectArgs.builder()

                    .bucket(bucket)

                    .object(objectName)

                    .build());

            log.debug("Removed MinIO seed object: {}/{}", bucket, objectName);

        }

    }



    private boolean syncOne(Path root, JsonNode entry) throws Exception {

        String localPath = entry.path("localPath").asText();

        String bucket = entry.path("bucket").asText("submissions");

        String objectKey = entry.path("objectKey").asText();

        String originalName = entry.path("originalName").asText();

        String contentType = entry.path("contentType").asText("application/octet-stream");

        long fileId = entry.path("id").asLong();



        Path localFile = root.resolve(localPath).normalize();

        if (!Files.isRegularFile(localFile)) {

            log.warn("Material file missing: {}", localFile);

            return false;

        }



        ensureBucket(bucket);



        long size = Files.size(localFile);

        try (InputStream is = Files.newInputStream(localFile)) {

            minioClient.putObject(PutObjectArgs.builder()

                    .bucket(bucket)

                    .object(objectKey)

                    .stream(is, size, -1)

                    .contentType(contentType)

                    .build());

        }

        log.info("Uploaded material: {} -> {}/{}", localFile.getFileName(), bucket, objectKey);



        upsertFileStorageRecord(fileId, bucket, objectKey, originalName, contentType, size);

        return true;

    }



    private void ensureBucket(String bucket) throws Exception {

        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());

        if (!exists) {

            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());

        }

    }



    private void upsertFileStorageRecord(long fileId, String bucket, String objectKey,

                                         String originalName, String contentType, long size) {

        FileStorage byId = fileStorageMapper.selectById(fileId);

        if (byId != null) {

            updateIfChanged(byId, bucket, objectKey, originalName, contentType, size);

            return;

        }



        FileStorage byKey = fileStorageMapper.selectOne(new LambdaQueryWrapper<FileStorage>()

                .eq(FileStorage::getObjectKey, objectKey));

        if (byKey != null) {

            if (!byKey.getId().equals(fileId)) {

                log.warn("Removing orphan file_storage id={} with conflicting object_key={}",

                        byKey.getId(), objectKey);

                fileStorageMapper.deleteById(byKey.getId());

            } else {

                updateIfChanged(byKey, bucket, objectKey, originalName, contentType, size);

                return;

            }

        }



        FileStorage storage = new FileStorage();

        storage.setId(fileId);

        storage.setBucket(bucket);

        storage.setObjectKey(objectKey);

        storage.setOriginalName(originalName);

        storage.setContentType(contentType);

        storage.setFileSize(size);

        storage.setAccessUrl("/api/v1/files/" + fileId + "/download");

        storage.setCreateTime(LocalDateTime.now());

        fileStorageMapper.insert(storage);

    }



    private void updateIfChanged(FileStorage existing, String bucket, String objectKey,

                                 String originalName, String contentType, long size) {

        boolean changed = !bucket.equals(existing.getBucket())

                || !objectKey.equals(existing.getObjectKey())

                || !originalName.equals(existing.getOriginalName())

                || !contentType.equals(existing.getContentType())

                || !Long.valueOf(size).equals(existing.getFileSize());



        if (!changed) {

            return;

        }



        existing.setBucket(bucket);

        existing.setObjectKey(objectKey);

        existing.setOriginalName(originalName);

        existing.setContentType(contentType);

        existing.setFileSize(size);

        existing.setAccessUrl("/api/v1/files/" + existing.getId() + "/download");

        fileStorageMapper.updateById(existing);

        log.debug("Updated file_storage id={} object_key={}", existing.getId(), objectKey);

    }

}

