package com.pposong.pposongbackend.dto.profile;

import com.pposong.pposongbackend.entity.User;

public class ProfileResponse {

    private Long id;
    private String username;
    private String profileImageUrl;
    private String bio;

    private long postCount;
    private long receivedLikeCount;

    public ProfileResponse(User user,
                           long postCount,
                           long receivedLikeCount) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.profileImageUrl = user.getProfileImageUrl();
        this.bio = user.getBio();

        this.postCount = postCount;
        this.receivedLikeCount = receivedLikeCount;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getBio() {
        return bio;
    }

    public long getPostCount() {
        return postCount;
    }

    public long getReceivedLikeCount() {
        return receivedLikeCount;
    }

}