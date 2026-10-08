package com.pposong.pposongbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImage(
            MultipartFile file
    ) {
        // 파일이 없는 경우
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "업로드할 이미지가 없습니다."
            );
        }

        // 이미지 파일인지 확인
        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "이미지 파일만 업로드할 수 있습니다."
            );
        }

        // 원본 파일명
        String originalFilename =
                file.getOriginalFilename();

        // 확장자 추출
        String extension = "";

        if (originalFilename != null &&
                originalFilename.contains(".")) {

            extension =
                    originalFilename.substring(
                            originalFilename.lastIndexOf(".")
                    );
        }

        // S3에 저장할 고유 파일명
        String fileName =
                "posts/"
                        + UUID.randomUUID()
                        + extension;

        try {
            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(contentType)
                            .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(
                            file.getBytes()
                    )
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "이미지 업로드에 실패했습니다.",
                    e
            );
        }

        return String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                fileName
        );
    }

    public void deleteImage(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String prefix =
                "https://" +
                        bucketName +
                        ".s3." +
                        region +
                        ".amazonaws.com/";

        if (!imageUrl.startsWith(prefix)) {
            return;
        }

        String key =
                imageUrl.substring(
                        prefix.length()
                );

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
    }
}