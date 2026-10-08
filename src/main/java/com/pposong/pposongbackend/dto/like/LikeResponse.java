package com.pposong.pposongbackend.dto.like;

public class LikeResponse {

    private final boolean liked;
    private final long likeCount;

    public LikeResponse(
            boolean liked,
            long likeCount
    ) {
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public long getLikeCount() {
        return likeCount;
    }
}