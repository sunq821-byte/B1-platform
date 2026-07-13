package com.b1.module.file.service;

import com.b1.module.file.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {

    FileUploadVO upload(MultipartFile file, String bucket);

    InputStream download(Long fileId);

    String getOriginalName(Long fileId);

    String getAccessUrl(Long fileId);

    /**
     * 读取文件内容并编码为 base64 data URI（形如 {@code data:image/png;base64,...}）。
     * 用于把图片内联传给需要"可直接读取图片"的外部服务（如通义千问视觉），
     * 避免依赖外部服务能访问到内网/本地 MinIO 的预签名 URL。
     */
    String getBase64DataUri(Long fileId);

    void delete(Long fileId);
}
