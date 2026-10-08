package com.pposong.pposongbackend.repository;

import com.pposong.pposongbackend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByActiveOrderByCreatedAtDesc(String active);

    List<Post> findAllByUserIdAndActiveOrderByCreatedAtDesc(
            Long userId,
            String active
    );

    long countByUserIdAndActive(
            Long userId,
            String active
    );
}