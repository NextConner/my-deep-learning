package com.claude.learn.repository;

import com.claude.learn.domain.DocumentSegment;

import java.util.List;

public interface DocumentRepository {

    List<DocumentSegment> fullTextSearchWithFilters(
            String query,
            int topK,
            String source,
            Object fromTime
    );
}