package com.pposong.pposongbackend.dto.post;

import com.pposong.pposongbackend.entity.Post;

import java.time.LocalDateTime;

import java.util.List;

public class PostResponse {

    private Long id;
    private String content;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
    private String profileImageUrl;

    // 좋아요 개수
    private long likeCount;

    // 내가 좋아요를 눌렀는지
    private boolean liked;

    // 댓글 개수
    private long commentCount;

    public PostResponse(
            Post post,
            long likeCount,
            boolean liked,
            long commentCount,
            List<String> imageUrls
    ) {
        this.id = post.getId();
        this.content = post.getContent();
        this.userId = post.getUser().getId();
        this.username = post.getUser().getUsername();

        System.out.println("===== 게시글 시간 확인 =====");
        System.out.println("게시글 ID: " + post.getId());
        System.out.println("Java createdAt: " + post.getCreatedAt());
        System.out.println("JVM 시간대: " + java.time.ZoneId.systemDefault());

        this.createdAt = post.getCreatedAt();
        this.createdAt = post.getCreatedAt();

        this.likeCount = likeCount;
        this.liked = liked;
        this.commentCount = commentCount;

        this.imageUrls = imageUrls;
        this.profileImageUrl =
                post.getUser().getProfileImageUrl();
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

    public long getLikeCount() {
        return likeCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}