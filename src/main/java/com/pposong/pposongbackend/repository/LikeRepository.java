package com.pposong.pposongbackend.repository;

import com.pposong.pposongbackend.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import com.pposong.pposongbackend.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository
        extends JpaRepository<Like, Long> {

    // 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 조회
    Optional<Like> findByPostIdAndUserId(
            Long postId,
            Long userId
    );

    // 특정 게시글의 전체 좋아요 개수
    long countByPostId(Long postId);

    void deleteAllByPostId(Long postId);

    long countByUserId(Long userId);

    @Query("""
    SELECT COUNT(l)
    FROM Like l
    WHERE l.post.user.id = :userId
      AND l.post.active = 'Y'
""")
    long countReceivedLikesByUserId(
            @Param("userId") Long userId
    );

    @Query("""
    SELECT l.post
    FROM Like l
    WHERE l.user.id = :userId
      AND l.post.active = 'Y'
    ORDER BY l.post.createdAt DESC
""")
    List<Post> findLikedPostsByUserId(
            @Param("userId") Long userId
    );
}