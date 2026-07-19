package com.b1.infrastructure.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "b1.minio")
public class MinioConfig {

    private static final List<String> REQUIRED_BUCKETS = List.of("submissions", "reports");

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private boolean secure;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureBuckets() {
        MinioClient client = minioClient();
        for (String bucket : REQUIRED_BUCKETS) {
            try {
                boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    log.info("MinIO bucket created: {}", bucket);
                } else {
                    log.info("MinIO bucket exists: {}", bucket);
                }
            } catch (Exception e) {
                log.error("Failed to ensure MinIO bucket: {}", bucket, e);
            }
        }
    }
}
