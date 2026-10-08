package com.pposong.pposongbackend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_images")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글의 이미지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "post_id",
            nullable = false
    )
    private Post post;

    // S3 이미지 주소
    @Column(
            name = "image_url",
            nullable = false,
            length = 1000
    )
    private String imageUrl;

    // 이미지 순서
    @Column(
            name = "image_order",
            nullable = false
    )
    private Integer imageOrder;

    protected PostImage() {
    }

    public PostImage(
            Post post,
            String imageUrl,
            Integer imageOrder
    ) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getImageOrder() {
        return imageOrder;
    }
}