package com.pposong.pposongbackend.repository;

import com.pposong.pposongbackend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository
        extends JpaRepository<Report, Long> {

    boolean existsByPostIdAndReporterId(
            Long postId,
            Long reporterId
    );
}