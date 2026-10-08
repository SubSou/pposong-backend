package com.pposong.pposongbackend.service;

import com.pposong.pposongbackend.dto.report.CreateReportRequest;
import com.pposong.pposongbackend.entity.Post;
import com.pposong.pposongbackend.entity.Report;
import com.pposong.pposongbackend.entity.User;
import com.pposong.pposongbackend.repository.PostRepository;
import com.pposong.pposongbackend.repository.ReportRepository;
import com.pposong.pposongbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public ReportService(
            ReportRepository reportRepository,
            PostRepository postRepository,
            UserRepository userRepository
    ) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createReport(
            Long userId,
            CreateReportRequest request
    ) {
        // 신고할 게시글 조회
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );

        // 삭제된 게시글인지 확인
        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "삭제된 게시글은 신고할 수 없습니다."
            );
        }

        // 신고한 사용자 조회
        User reporter = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        // 자신의 게시글 신고 방지
        if (post.getUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    "자신의 게시글은 신고할 수 없습니다."
            );
        }

        // 중복 신고 방지
        if (reportRepository.existsByPostIdAndReporterId(
                post.getId(),
                userId
        )) {
            throw new IllegalStateException(
                    "이미 신고한 게시글입니다."
            );
        }

        // 신고 생성
        Report report = new Report(
                post,
                reporter,
                request.getReason()
        );

        // DB 저장
        reportRepository.save(report);
    }
}