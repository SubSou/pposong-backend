package com.pposong.pposongbackend.controller;

import com.pposong.pposongbackend.dto.report.CreateReportRequest;
import com.pposong.pposongbackend.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(
            ReportService reportService
    ) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createReport(
            @Valid @RequestBody CreateReportRequest request,
            Authentication authentication
    ) {
        Long userId =
                (Long) authentication.getPrincipal();

        reportService.createReport(
                userId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "message",
                                "게시글 신고가 접수되었습니다."
                        )
                );
    }
}