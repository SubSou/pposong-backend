package com.pposong.pposongbackend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreatePostRequest {

    @NotBlank
    @Size(max = 1000)
    private String content;

    @Size(max = 10)
    private List<String> imageUrls;

    public String getContent() {
        return content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}