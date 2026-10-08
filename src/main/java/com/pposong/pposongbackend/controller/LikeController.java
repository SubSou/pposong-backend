package com.pposong.pposongbackend.controller;

import com.pposong.pposongbackend.dto.like.LikeResponse;
import com.pposong.pposongbackend.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class LikeController {

    private final LikeService likeService;

    public LikeController(
            LikeService likeService
    ) {
        this.likeService = likeService;
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        LikeResponse response =
                likeService.toggleLike(
                        userId,
                        postId
                );

        return ResponseEntity.ok(response);
    }
}