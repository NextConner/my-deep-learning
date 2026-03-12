package com.claude.learn.repository;

import com.claude.learn.domain.DocumentSegment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository {

    List<DocumentSegment> fullTextSearchWithFilters(
            String query,
            int topK,
            String source,
            Object fromTime
    );

    @Query(value = """
            SELECT id, content, source, doc_type, created_at
            FROM document_segments
            WHERE ts_content @@ plainto_tsquery('simple', :keyword)
            ORDER BY ts_rank(ts_content, plainto_tsquery('simple', :keyword)) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentSegment> fullTextSearch(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );
}