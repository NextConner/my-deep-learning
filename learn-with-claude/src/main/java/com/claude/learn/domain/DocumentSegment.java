package com.claude.learn.domain;


import jakarta.persistence.*;

@Entity
@Table(name = "document_segments")
public class DocumentSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String source;

    // 构造器
    public DocumentSegment() {}

    public DocumentSegment(String content, String source) {
        this.content = content;
        this.source = source;
    }

    // Getter
    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getSource() { return source; }
}