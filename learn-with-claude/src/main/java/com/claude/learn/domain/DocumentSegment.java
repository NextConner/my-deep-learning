package com.claude.learn.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_segments")
public class DocumentSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String source;

    @Column(name = "doc_type")
    private String docType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DocumentSegment() {
    }

    public DocumentSegment(String content, String source) {
        this.content = content;
        this.source = source;
    }

    public DocumentSegment(String content, String source, String docType) {
        this.content = content;
        this.source = source;
        this.docType = docType;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getSource() {
        return source;
    }

    public String getDocType() {
        return docType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
