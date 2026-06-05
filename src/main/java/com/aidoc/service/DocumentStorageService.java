package com.aidoc.service;

import com.aidoc.config.FileStorageProperties;
import com.aidoc.entity.DocumentInfo;
import com.aidoc.repository.DocumentInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentStorageService {

    private static final String PDF_SUFFIX = ".pdf";
    private static final int PROCESS_STATUS_UPLOADED = 0;

    private final FileStorageProperties fileStorageProperties;
    private final DocumentInfoRepository documentInfoRepository;

    private Path uploadRoot;

    @PostConstruct
    void initUploadDirectory() throws IOException {
        uploadRoot = Paths.get(fileStorageProperties.getDir()).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
    }

    public DocumentInfo saveUploadedPdf(MultipartFile file) throws IOException {
        validatePdfFile(file);

        String originalFileName = sanitizeOriginalFileName(file.getOriginalFilename());
        String storedFileName = buildStoredFileName(originalFileName);
        Path targetPath = uploadRoot.resolve(storedFileName).normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("非法的文件路径");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        DocumentInfo document = DocumentInfo.builder()
                .fileName(originalFileName)
                .fileSize(file.getSize())
                .storagePath(targetPath.toAbsolutePath().toString())
                .processStatus(PROCESS_STATUS_UPLOADED)
                .build();

        return documentInfoRepository.save(document);
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            throw new IllegalArgumentException("文件名无效");
        }
        String lowerName = originalFileName.trim().toLowerCase();
        if (!lowerName.endsWith(PDF_SUFFIX)) {
            throw new IllegalArgumentException("仅支持上传 .pdf 格式的文件");
        }
    }

    private String sanitizeOriginalFileName(String originalFileName) {
        String fileName = Paths.get(originalFileName.trim()).getFileName().toString();
        if (!StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("文件名无效");
        }
        return fileName;
    }

    private String buildStoredFileName(String originalFileName) {
        String baseName = originalFileName;
        if (baseName.toLowerCase().endsWith(PDF_SUFFIX)) {
            baseName = baseName.substring(0, baseName.length() - PDF_SUFFIX.length());
        }
        return System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + baseName + PDF_SUFFIX;
    }
}
