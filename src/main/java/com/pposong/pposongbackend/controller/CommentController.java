package com.pposong.pposongbackend.controller;

import com.pposong.pposongbackend.dto.comment.CommentResponse;
import com.pposong.pposongbackend.dto.comment.CreateCommentRequest;
import com.pposong.pposongbackend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.pposong.pposongbackend.dto.comment.UpdateCommentRequest;

@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(
            CommentService commentService
    ) {
        this.commentService = commentService;
    }

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        CommentResponse response =
                commentService.createComment(
                        userId,
                        postId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // 댓글 목록 조회
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId
    ) {
        List<CommentResponse> comments =
                commentService.getComments(postId);

        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        commentService.deleteComment(
                userId,
                commentId
        );

        return ResponseEntity.noContent().build();
    }

    // 댓글 수정
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        CommentResponse response =
                commentService.updateComment(
                        userId,
                        commentId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}