package com.pposong.pposongbackend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateCommentRequest {

    @NotBlank
    @Size(max = 500)
    private String content;

    public String getContent() {
        return content;
    }
}