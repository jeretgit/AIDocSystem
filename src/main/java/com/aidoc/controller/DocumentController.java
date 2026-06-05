package com.aidoc.controller;


import com.aidoc.dto.ApiResponse;
import com.aidoc.dto.DocumentUploadData;
import com.aidoc.entity.DocumentInfo;
import com.aidoc.repository.DocumentInfoRepository;
import com.aidoc.service.DocumentService;
import com.aidoc.service.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentStorageService documentStorageService;
    private final DocumentService documentService;
    private final DocumentInfoRepository documentInfoRepository;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentUploadData>> upload(
            @RequestParam("file") MultipartFile file) {
        try {
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentInfo>> getDocument(@PathVariable Long id) {
        return documentInfoRepository.findById(id)
                .map(document -> ResponseEntity.ok(ApiResponse.ok("查询成功", document)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail("文档不存在")));
    }
}



