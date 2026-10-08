package com.pposong.pposongbackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 댓글 내용
    @Column(nullable = false, length = 500)
    private String content;

    // 댓글이 작성된 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 댓글 작성 시간
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 삭제 여부
    @Column(nullable = false, length = 1)
    private String active = "Y";

    @Column(length = 100)
    private String mentionUsername;

    protected Comment() {
    }

    public Comment(
            String content,
            Post post,
            User user
    ) {
        this.content = content;
        this.post = post;
        this.user = user;
    }

    public Comment(
            String content,
            Post post,
            User user,
            Comment parent,
            String mentionUsername
    ) {
        this.content = content;
        this.post = post;
        this.user = user;
        this.parent = parent;
        this.mentionUsername = mentionUsername;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.active = "N";
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Post getPost() {
        return post;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getActive() {
        return active;
    }

    public Comment getParent() {
        return parent;
    }

    public String getMentionUsername() {
        return mentionUsername;
    }
}