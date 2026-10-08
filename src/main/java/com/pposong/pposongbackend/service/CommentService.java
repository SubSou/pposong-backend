package com.pposong.pposongbackend.service;

import com.pposong.pposongbackend.dto.comment.CommentResponse;
import com.pposong.pposongbackend.dto.comment.CreateCommentRequest;
import com.pposong.pposongbackend.entity.Comment;
import com.pposong.pposongbackend.entity.Post;
import com.pposong.pposongbackend.entity.User;
import com.pposong.pposongbackend.repository.CommentRepository;
import com.pposong.pposongbackend.repository.PostRepository;
import com.pposong.pposongbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.pposong.pposongbackend.dto.comment.UpdateCommentRequest;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(
            Long userId,
            Long postId,
            CreateCommentRequest request
    ) {
        Post post = postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );

        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "삭제된 게시글에는 댓글을 작성할 수 없습니다."
            );
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        Comment comment;

        // 일반 댓글
        if (request.getParentId() == null) {

            comment = new Comment(
                    request.getContent(),
                    post,
                    user
            );

        } else {

            // 대댓글
            Comment parentComment =
                    commentRepository
                            .findById(
                                    request.getParentId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "부모 댓글을 찾을 수 없습니다."
                                    )
                            );

            if ("N".equals(parentComment.getActive())) {
                throw new IllegalStateException(
                        "삭제된 댓글에는 답글을 작성할 수 없습니다."
                );
            }

            if (!parentComment
                    .getPost()
                    .getId()
                    .equals(postId)) {

                throw new IllegalStateException(
                        "잘못된 부모 댓글입니다."
                );
            }

            // 대댓글의 대댓글 방지
            if (parentComment.getParent() != null) {
                throw new IllegalStateException(
                        "대댓글에는 답글을 작성할 수 없습니다."
                );
            }

            comment = new Comment(
                    request.getContent(),
                    post,
                    user,
                    parentComment,
                    request.getMentionUsername()
            );
        }

        Comment savedComment =
                commentRepository.save(comment);

        return new CommentResponse(savedComment);
    }

    // 특정 게시글 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(
            Long postId
    ) {
        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );

        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "삭제된 게시글의 댓글은 조회할 수 없습니다."
            );
        }

        return commentRepository
                .findAllByPostIdAndActiveOrderByCreatedAtAsc(
                        postId,
                        "Y"
                )
                .stream()
                .map(CommentResponse::new)
                .toList();
    }

    @Transactional
    public void deleteComment(
            Long userId,
            Long commentId
    ) {
        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "댓글을 찾을 수 없습니다."
                        )
                );

        if ("N".equals(comment.getActive())) {
            throw new IllegalStateException(
                    "이미 삭제된 댓글입니다."
            );
        }

        if (!comment.getUser()
                .getId()
                .equals(userId)) {
            throw new IllegalStateException(
                    "댓글을 삭제할 권한이 없습니다."
            );
        }

        // 일반 댓글인 경우
        if (comment.getParent() == null) {

            // 해당 댓글의 대댓글 조회
            List<Comment> replies =
                    commentRepository
                            .findAllByParentIdAndActive(
                                    commentId,
                                    "Y"
                            );

            // 대댓글도 모두 삭제 처리
            for (Comment reply : replies) {
                reply.delete();
            }
        }

        // 현재 댓글 삭제
        comment.delete();
    }

    @Transactional
    public CommentResponse updateComment(
            Long userId,
            Long commentId,
            UpdateCommentRequest request
    ) {
        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "댓글을 찾을 수 없습니다."
                        )
                );

        // 삭제된 댓글인지 확인
        if ("N".equals(comment.getActive())) {
            throw new IllegalStateException(
                    "삭제된 댓글은 수정할 수 없습니다."
            );
        }

        // 작성자 본인인지 확인
        if (!comment.getUser()
                .getId()
                .equals(userId)) {
            throw new IllegalStateException(
                    "댓글을 수정할 권한이 없습니다."
            );
        }

        // 댓글 내용 수정
        comment.updateContent(
                request.getContent()
        );

        return new CommentResponse(comment);
    }
}