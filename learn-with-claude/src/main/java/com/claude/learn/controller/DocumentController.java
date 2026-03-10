package com.claude.learn.controller;

import com.claude.learn.service.DocumentIngestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    DocumentIngestService documentIngestService;

    @PreAuthorize("hasAnyRole('AI_ADMIN', 'AI_OPS')")
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            log.info("📄 收到上传文件：{}", filename);
            documentIngestService.ingest(file.getBytes(), filename);
            return ResponseEntity.ok(Map.of("message", "上传成功", "filename", filename));
        } catch (Exception e) {
            log.error("❌ 上传失败：", e);  // 打印完整堆栈
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
