package com.b1.module.file.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.module.file.entity.FileStorage;
import com.b1.module.file.mapper.FileStorageMapper;
import com.b1.module.file.service.FileService;
import com.b1.module.file.vo.FileUploadVO;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final FileStorageMapper fileStorageMapper;

    @Value("${b1.upload.max-size:52428800}")
    private long maxSize;

    @Value("${b1.upload.allowed-types:zip,pdf,doc,docx,xls,xlsx,java,py,c,cpp,txt,md,png,jpg,jpeg}")
    private String allowedTypes;

    private static final long PRESIGNED_EXPIRE_MINUTES = 15;

    @Override
    public FileUploadVO upload(MultipartFile file, String bucket) {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        String objectKey = String.format("%s/%s/%s.%s",
                bucket,
                java.time.LocalDate.now().toString().replace("-", "/"),
                IdUtil.fastSimpleUUID(),
                ext);

        try {
            byte[] bytes = file.getBytes();
            String md5 = DigestUtil.md5Hex(bytes);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(file.getInputStream(), bytes.length, -1)
                    .contentType(file.getContentType())
                    .build());

            FileStorage storage = new FileStorage();
            storage.setBucket(bucket);
            storage.setObjectKey(objectKey);
            storage.setOriginalName(originalName);
            storage.setContentType(file.getContentType());
            storage.setFileSize(file.getSize());
            storage.setFileMd5(md5);
            fileStorageMapper.insert(storage);

            String accessUrl = generatePresignedUrl(bucket, objectKey);

            FileUploadVO vo = new FileUploadVO();
            vo.setFileId(storage.getId());
            vo.setUrl(accessUrl);
            vo.setOriginalName(originalName);
            vo.setFileSize(file.getSize());
            return vo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, objectKey={}", bucket, objectKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public InputStream download(Long fileId) {
        FileStorage storage = getById(fileId);
        try {
            return minioClient.getObject(
                    io.minio.GetObjectArgs.builder()
                            .bucket(storage.getBucket())
                            .object(storage.getObjectKey())
                            .build());
        } catch (Exception e) {
            log.error("文件下载失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public String getOriginalName(Long fileId) {
        return getById(fileId).getOriginalName();
    }

    @Override
    public String getAccessUrl(Long fileId) {
        FileStorage storage = getById(fileId);
        return generatePresignedUrl(storage.getBucket(), storage.getObjectKey());
    }

    @Override
    public String getBase64DataUri(Long fileId) {
        FileStorage storage = getById(fileId);
        try (InputStream is = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                        .bucket(storage.getBucket())
                        .object(storage.getObjectKey())
                        .build())) {
            byte[] bytes = is.readAllBytes();
            String contentType = (storage.getContentType() != null && !storage.getContentType().isBlank())
                    ? storage.getContentType() : "application/octet-stream";
            return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("生成图片 base64 data URI 失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public void delete(Long fileId) {
        FileStorage storage = fileStorageMapper.selectById(fileId);
        if (storage == null) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(storage.getBucket())
                    .object(storage.getObjectKey())
                    .build());
        } catch (Exception e) {
            log.warn("MinIO 文件删除失败: bucket={}, objectKey={}", storage.getBucket(), storage.getObjectKey(), e);
        }
        fileStorageMapper.deleteById(fileId);
    }

    private FileStorage getById(Long fileId) {
        FileStorage storage = fileStorageMapper.selectById(fileId);
        if (storage == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return storage;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
        String ext = FileUtil.extName(file.getOriginalFilename());
        Set<String> allowed = Set.of(allowedTypes.split(","));
        if (!allowed.contains(ext.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_UNSUPPORTED);
        }
    }

    private String generatePresignedUrl(String bucket, String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry((int) PRESIGNED_EXPIRE_MINUTES, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.error("生成预签名URL失败: bucket={}, objectKey={}", bucket, objectKey, e);
            return null;
        }
    }
}
