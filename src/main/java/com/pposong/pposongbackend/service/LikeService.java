package com.pposong.pposongbackend.service;

import com.pposong.pposongbackend.entity.Like;
import com.pposong.pposongbackend.entity.Post;
import com.pposong.pposongbackend.entity.User;
import com.pposong.pposongbackend.repository.LikeRepository;
import com.pposong.pposongbackend.repository.PostRepository;
import com.pposong.pposongbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import com.pposong.pposongbackend.dto.like.LikeResponse;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeService(
            LikeRepository likeRepository,
            PostRepository postRepository,
            UserRepository userRepository
    ) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public LikeResponse toggleLike(
            Long userId,
            Long postId
    ) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );

        // 삭제된 게시글 확인
        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "삭제된 게시글에는 좋아요를 누를 수 없습니다."
            );
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        // 기존 좋아요 확인
        Optional<Like> existingLike =
                likeRepository.findByPostIdAndUserId(
                        postId,
                        userId
                );

        // 이미 좋아요를 눌렀다면 → 좋아요 취소
        if (existingLike.isPresent()) {

            likeRepository.delete(existingLike.get());

            long likeCount =
                    likeRepository.countByPostId(postId);

            return new LikeResponse(
                    false,
                    likeCount
            );
        }

        // 좋아요가 없다면 → 좋아요 추가
        Like like = new Like(
                post,
                user
        );

        likeRepository.save(like);

        long likeCount =
                likeRepository.countByPostId(postId);

        return new LikeResponse(
                true,
                likeCount
        );
    }
}