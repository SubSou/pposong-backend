package com.pposong.pposongbackend.repository;

import com.pposong.pposongbackend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPostIdAndActiveOrderByCreatedAtAsc(
            Long postId,
            String active
    );

    long countByPostIdAndActive(
            Long postId,
            String active
    );

    List<Comment> findAllByParentIdAndActive(
            Long parentId,
            String active
    );
}