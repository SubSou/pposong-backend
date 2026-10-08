package com.pposong.pposongbackend.controller;

import com.pposong.pposongbackend.dto.post.CreatePostRequest;
import com.pposong.pposongbackend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.pposong.pposongbackend.dto.post.PostResponse;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.pposong.pposongbackend.dto.post.UpdatePostRequest;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/liked")
    public ResponseEntity<List<PostResponse>> getLikedPosts(
            Authentication authentication
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        List<PostResponse> response =
                postService.getLikedPosts(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PostResponse>> getMyPosts(
            Authentication authentication
    ) {

        Long userId =
                (Long) authentication.getPrincipal();

        List<PostResponse> response =
                postService.getMyPosts(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        postService.createPost(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "게시글이 작성되었습니다."
                ));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getPosts(
            Authentication authentication
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        List<PostResponse> posts =
                postService.getPosts(userId);

        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        postService.deletePost(userId, postId);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "게시글이 삭제되었습니다."
                )
        );
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<Map<String, String>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        postService.updatePost(
                userId,
                postId,
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "게시글이 수정되었습니다."
                )
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        PostResponse post =
                postService.getPost(
                        userId,
                        postId
                );

        return ResponseEntity.ok(post);
    }
}