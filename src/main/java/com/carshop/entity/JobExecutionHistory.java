package com.carshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_execution_history", indexes = {
        @Index(name = "idx_job_execution_job_name", columnList = "job_name"),
        @Index(name = "idx_job_execution_started_at", columnList = "started_at"),
        @Index(name = "idx_job_execution_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class JobExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private JobTriggerType triggerType;

    @Column(name = "triggered_by", nullable = false, length = 100)
    private String triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobExecutionStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "processed_count", nullable = false)
    @Builder.Default
    private long processedCount = 0;

    @Column(name = "success_count", nullable = false)
    @Builder.Default
    private long successCount = 0;

    @Column(name = "failed_count", nullable = false)
    @Builder.Default
    private long failedCount = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
