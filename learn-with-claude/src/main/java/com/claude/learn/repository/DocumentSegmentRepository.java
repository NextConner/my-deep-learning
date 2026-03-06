package com.claude.learn.repository;


import com.claude.learn.domain.DocumentSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentSegmentRepository extends JpaRepository<DocumentSegment, Long> {

    // BM25 全文检索
    @Query(value = """
            SELECT id, content, source
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
