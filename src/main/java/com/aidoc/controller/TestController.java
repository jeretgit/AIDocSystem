package com.aidoc.controller;

import com.aidoc.entity.DocumentInfo;
import com.aidoc.repository.DocumentInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final DocumentInfoRepository documentInfoRepository;

    @GetMapping("/insert")
    public String insertTestDocument() {
        DocumentInfo document = DocumentInfo.builder()
                .fileName("测试文档.pdf")
                .fileSize(1024L)
                .storagePath("/test/storage/测试文档.pdf")
                .processStatus(0)
                .globalSummary("这是一条测试文档记录")
                .build();

        documentInfoRepository.save(document);
        return "插入成功";
    }
}
