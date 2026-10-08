package com.pposong.pposongbackend.websocket.dto;

public class OnlineUserResponse {

    private Long userId;
    private String username;
    private String profileImageUrl;

    public OnlineUserResponse(
            Long userId,
            String username,
            String profileImageUrl
    ) {
        this.userId = userId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}