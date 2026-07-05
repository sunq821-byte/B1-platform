package com.b1.module.file.service;

import com.b1.module.file.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {

    FileUploadVO upload(MultipartFile file, String bucket);

    InputStream download(Long fileId);

    String getOriginalName(Long fileId);

    String getAccessUrl(Long fileId);

    void delete(Long fileId);
}
