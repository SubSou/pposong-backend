package com.pposong.pposongbackend.controller;

import com.pposong.pposongbackend.dto.profile.ProfileResponse;
import com.pposong.pposongbackend.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pposong.pposongbackend.dto.profile.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(
            ProfileService profileService
    ) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            Authentication authentication
    ) {

        Long userId =
                (Long) authentication.getPrincipal();

        ProfileResponse response =
                profileService.getMyProfile(userId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        profileService.updateMyProfile(
                userId,
                request
        );

        return ResponseEntity.noContent().build();
    }
}