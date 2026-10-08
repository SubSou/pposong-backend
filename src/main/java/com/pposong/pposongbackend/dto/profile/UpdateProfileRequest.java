package com.pposong.pposongbackend.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    private String username;

    @Size(max = 100, message = "소개는 100자 이하로 입력해주세요.")
    private String bio;

    private String profileImageUrl;

    public String getUsername() {
        return username;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}