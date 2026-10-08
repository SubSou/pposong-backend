package com.pposong.pposongbackend.dto.auth;

import com.pposong.pposongbackend.entity.User;

public class UserResponse {

    private final Long id;
    private final String username;
    private final String email;
    private final String role;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole().name();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}