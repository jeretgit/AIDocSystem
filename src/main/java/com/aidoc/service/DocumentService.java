package com.aidoc.service;

import com.aidoc.entity.DocumentInfo;
import com.aidoc.repository.DocumentInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final int PROCESS_STATUS_PARSED = 1;
    private static final int PROCESS_STATUS_SUMMARY_COMPLETED = 2;
    private static final int PROCESS_STATUS_PARSE_FAILED = 3;
    private static final int TEXT_PREVIEW_LENGTH = 500;
    private static final Duration CACHE_EXPIRY = Duration.ofDays(7);

    private final DocumentInfoRepository documentInfoRepository;
    private final AiService aiService;
    private final StringRedisTemplate stringRedisTemplate;

    @Async
    public void processDocument(Long documentId) {
        try {
            DocumentInfo document = documentInfoRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalArgumentException("文档不存在, id=" + documentId));

            // Calculate MD5 of the PDF file
            String md5 = calculateFileMd5(document.getStoragePath());
            String cacheKey = "doc:summary:" + md5;

            // Check Redis cache
            String cachedSummary = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedSummary != null) {
                System.out.println("=== 命中 Redis 缓存！ ===");
                System.out.println("=== AI 总结结果 ===");
                System.out.println(cachedSummary);
                
                document.setGlobalSummary(cachedSummary);
                document.setProcessStatus(PROCESS_STATUS_SUMMARY_COMPLETED);
                documentInfoRepository.save(document);
                System.out.println("=== 文档 ID: " + documentId + " 全链路处理彻底完成，数据已落盘 ===");
                log.info("PDF 解析完成 (缓存命中), documentId={}", documentId);
                return;
            }

            // Cache miss - process normally
            String extractedText = extractTextFromPdf(document.getStoragePath());
            printTextPreview(documentId, extractedText);

            List<String> chunks = chunkText(extractedText, 1000, 100);
            System.out.println("=== 切片完成，共分为 [" + chunks.size() + "] 块 ===");
            if (chunks.size() >= 1) {
                System.out.println("第 1 块的前 50 个字符：" + chunks.get(0).substring(0, Math.min(50, chunks.get(0).length())));
            }
            if (chunks.size() >= 2) {
                System.out.println("第 2 块的前 50 个字符：" + chunks.get(1).substring(0, Math.min(50, chunks.get(1).length())));
                System.out.println("第 2 块的重叠内容验证：" + chunks.get(1).substring(0, Math.min(100, chunks.get(1).length())));
            }

            if (!extractedText.isEmpty()) {
                String textForSummary = extractedText.substring(0, Math.min(extractedText.length(), 60000));
                String summary = aiService.summarizeChunk(textForSummary);
                System.out.println("=== AI 总结结果 ===");
                System.out.println(summary);
                
                // Cache the result in Redis with 7-day expiry
                stringRedisTemplate.opsForValue().set(cacheKey, summary, CACHE_EXPIRY);
                
                document.setGlobalSummary(summary);
                document.setProcessStatus(PROCESS_STATUS_SUMMARY_COMPLETED);
                documentInfoRepository.save(document);
                System.out.println("=== 文档 ID: " + documentId + " 全链路处理彻底完成，数据已落盘 ===");
            } else {
                document.setProcessStatus(PROCESS_STATUS_PARSED);
                documentInfoRepository.save(document);
            }
            log.info("PDF 解析完成, documentId={}", documentId);
        } catch (Exception ex) {
            log.error("PDF 解析失败, documentId={}", documentId, ex);
            markParseFailed(documentId);
        }
    }

    private String extractTextFromPdf(String storagePath) throws IOException {
        Path pdfPath = Paths.get(storagePath).normalize();
        if (!Files.isRegularFile(pdfPath)) {
            throw new IOException("PDF 文件不存在或不可读: " + storagePath);
        }

        File pdfFile = pdfPath.toFile();
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text != null ? text : "";
        }
    }

    private void printTextPreview(Long documentId, String extractedText) {
        String preview = extractedText.length() > TEXT_PREVIEW_LENGTH
                ? extractedText.substring(0, TEXT_PREVIEW_LENGTH)
                : extractedText;
        System.out.println("========== PDF 文本预览 (documentId=" + documentId + ") ==========");
        System.out.println(preview);
        System.out.println("========== 预览结束，总字符数: " + extractedText.length() + " ==========");
    }

    private void markParseFailed(Long documentId) {
        documentInfoRepository.findById(documentId).ifPresent(document -> {
            document.setProcessStatus(PROCESS_STATUS_PARSE_FAILED);
            documentInfoRepository.save(document);
        });
    }

    private List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        int position = 0;
        
        while (position < text.length()) {
            int end = Math.min(position + chunkSize, text.length());
            String chunk = text.substring(position, end);
            chunks.add(chunk);
            position += step;
        }
        
        return chunks;
    }

    private String calculateFileMd5(String filePath) throws IOException {
        Path path = Paths.get(filePath).normalize();
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            return DigestUtils.md5DigestAsHex(fis);
        }
    }
}
