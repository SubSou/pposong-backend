package com.pposong.pposongbackend.dto.report;

import com.pposong.pposongbackend.entity.ReportReason;
import jakarta.validation.constraints.NotNull;

public class CreateReportRequest {

    @NotNull(message = "게시글을 선택해주세요.")
    private Long postId;

    @NotNull(message = "신고 사유를 선택해주세요.")
    private ReportReason reason;

    public Long getPostId() {
        return postId;
    }

    public ReportReason getReason() {
        return reason;
    }
}