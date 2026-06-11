package com.aidoc.controller;


import com.aidoc.dto.ApiResponse;
import com.aidoc.dto.DocumentUploadData;
import com.aidoc.entity.DocumentInfo;
import com.aidoc.repository.DocumentInfoRepository;
import com.aidoc.service.DocumentService;
import com.aidoc.service.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin("*")
@RestController
@Slf4j
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentStorageService documentStorageService;
    private final DocumentService documentService;
    private final DocumentInfoRepository documentInfoRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentUploadData>> upload(
            @RequestParam("file") MultipartFile file) {
        try {
            // Step 1: Get byte stream
            byte[] fileBytes = file.getBytes();

            // Step 2: Extract text synchronously for fast-fail validation
            String extractedText = extractTextFromPdf(fileBytes);
            if (extractedText == null || extractedText.trim().length() < 50) {
                log.warn("检测到扫描版/图片型 PDF，拒绝处理");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail("文件解析失败：检测到该文件可能为图片或扫描版 PDF，暂不支持自动 OCR，请上传可编辑的文本版 PDF。"));
            }

            // Step 3: Calculate MD5 fingerprint
            String md5 = DigestUtils.md5DigestAsHex(fileBytes);
            String cacheKey = "doc:summary:" + md5;

            // Step 4: Check Redis immediately
            String cachedSummary = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedSummary != null) {
                // Cache hit - DO NOT store fileBytes in database!
                log.info("==========命中 Redis 缓存！=============");
                DocumentInfo document = DocumentInfo.builder()
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .fileContent(null) // Explicitly set to null to avoid storing binary
                        .processStatus(2) // PROCESS_STATUS_SUMMARY_COMPLETED
                        .globalSummary(cachedSummary)
                        .build();
                DocumentInfo saved = documentInfoRepository.save(document);

                DocumentUploadData data = DocumentUploadData.builder()
                        .documentId(saved.getId())
                        .build();
                return ResponseEntity.ok(
                        ApiResponse.ok("文件上传成功（缓存命中）", data));
            }

            // Cache miss - NOW allow storing complete fileBytes in database
            DocumentInfo saved = documentStorageService.saveUploadedPdf(file);
            documentService.processDocument(saved.getId());

            DocumentUploadData data = DocumentUploadData.builder()
                    .documentId(saved.getId())
                    .build();
            return ResponseEntity.ok(
                    ApiResponse.ok("文件上传成功", data));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("文件保存失败，请稍后重试"));
        }
    }

    private String extractTextFromPdf(byte[] fileContent) throws IOException {
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null ? text : "";
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentInfo>> getDocument(@PathVariable Long id) {
        return documentInfoRepository.findById(id)
                .map(document -> ResponseEntity.ok(ApiResponse.ok("查询成功", document)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail("文档不存在")));
    }
}



