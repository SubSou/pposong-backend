package com.pposong.pposongbackend.repository;

import com.pposong.pposongbackend.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository
        extends JpaRepository<PostImage, Long> {

    List<PostImage> findAllByPostIdOrderByImageOrderAsc(
            Long postId
    );

    void deleteAllByPostId(Long postId);
}