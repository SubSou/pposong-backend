package com.pposong.pposongbackend.service;

import com.pposong.pposongbackend.dto.profile.ProfileResponse;
import com.pposong.pposongbackend.entity.User;
import com.pposong.pposongbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pposong.pposongbackend.repository.PostRepository;
import com.pposong.pposongbackend.repository.LikeRepository;

import com.pposong.pposongbackend.dto.profile.UpdateProfileRequest;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    private final S3Service s3Service;

    public ProfileService(
            UserRepository userRepository,
            PostRepository postRepository,
            LikeRepository likeRepository,
            S3Service s3Service
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.s3Service = s3Service;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(
            Long userId
    ) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        long postCount =
                postRepository.countByUserIdAndActive(
                        userId,
                        "Y"
                );

        long receivedLikeCount =
                likeRepository.countReceivedLikesByUserId(
                        userId
                );

        return new ProfileResponse(
                user,
                postCount,
                receivedLikeCount
        );
    }

    @Transactional
    public void updateMyProfile(
            Long userId,
            UpdateProfileRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        String oldProfileImageUrl =
                user.getProfileImageUrl();

        String newProfileImageUrl =
                request.getProfileImageUrl();

        // 기존 사진과 새 사진이 다른 경우
        if (oldProfileImageUrl != null
                && !oldProfileImageUrl.isBlank()
                && !oldProfileImageUrl.equals(newProfileImageUrl)) {

            s3Service.deleteImage(
                    oldProfileImageUrl
            );
        }

        user.updateProfile(
                request.getUsername(),
                request.getBio(),
                newProfileImageUrl
        );
    }
}