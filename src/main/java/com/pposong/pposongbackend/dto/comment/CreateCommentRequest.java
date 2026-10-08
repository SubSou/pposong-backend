package com.pposong.pposongbackend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {

    @NotBlank
    @Size(max = 500)
    private String content;

    private Long parentId;

    @Size(max = 100)
    private String mentionUsername;

    public String getContent() {
        return content;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getMentionUsername() {
        return mentionUsername;
    }
}