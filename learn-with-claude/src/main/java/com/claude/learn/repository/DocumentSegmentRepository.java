package com.claude.learn.repository;

import com.claude.learn.domain.DocumentSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentSegmentRepository extends JpaRepository<DocumentSegment, Long> {

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

    @Query(value = """
            SELECT id, content, source, doc_type, created_at
            FROM document_segments
            WHERE ts_content @@ plainto_tsquery('simple', :keyword)
              AND (CAST(:source AS TEXT) IS NULL OR source = CAST(:source AS TEXT))
              AND (CAST(:fromTime AS TIMESTAMP) IS NULL OR created_at >= CAST(:fromTime AS TIMESTAMP))
            ORDER BY ts_rank(ts_content, plainto_tsquery('simple', :keyword)) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentSegment> fullTextSearchWithFilters(
            @Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("source") String source,
            @Param("fromTime") LocalDateTime fromTime
    );

    @Query("""
            SELECT COUNT(ds)
            FROM DocumentSegment ds
            WHERE ds.content = :content
              AND (:source IS NULL OR ds.source = :source)
              AND (:fromTime IS NULL OR ds.createdAt >= :fromTime)
            """)
    long countByContentWithFilters(
            @Param("content") String content,
            @Param("source") String source,
            @Param("fromTime") LocalDateTime fromTime
    );
}
