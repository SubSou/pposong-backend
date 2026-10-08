package com.pposong.pposongbackend.dto.comment;

import com.pposong.pposongbackend.entity.Comment;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private String content;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private Long parentId;

    // 누구에게 답글을 작성했는지
    private String mentionUsername;

    private String profileImageUrl;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.userId = comment.getUser().getId();
        this.username = comment.getUser().getUsername();
        this.createdAt = comment.getCreatedAt();

        this.parentId =
                comment.getParent() != null
                        ? comment.getParent().getId()
                        : null;

        this.mentionUsername =
                comment.getMentionUsername();
        this.profileImageUrl =
                comment.getUser().getProfileImageUrl();
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getMentionUsername() {
        return mentionUsername;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}