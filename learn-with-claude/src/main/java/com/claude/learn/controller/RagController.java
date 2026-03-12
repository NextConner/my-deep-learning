package com.claude.learn.controller;

import com.claude.learn.service.HybridSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final HybridSearchService hybridSearchService;

    public RagController(HybridSearchService hybridSearchService) {
        this.hybridSearchService = hybridSearchService;
    }

    @GetMapping("/search")
    public List<String> search(
            @RequestParam String query) {

        return hybridSearchService.hybridSearch(
                query,
                5
        );
    }
}
