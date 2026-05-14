package com.carshop.dto.response;

import com.carshop.entity.JobExecutionHistory;
import com.carshop.entity.JobExecutionStatus;
import com.carshop.entity.JobTriggerType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class JobExecutionResponse {
    Long id;
    String jobName;
    JobTriggerType triggerType;
    String triggeredBy;
    JobExecutionStatus status;
    LocalDateTime startedAt;
    LocalDateTime finishedAt;
    long processedCount;
    long successCount;
    long failedCount;
    String summary;
    String errorMessage;

    public static JobExecutionResponse from(JobExecutionHistory execution) {
        return JobExecutionResponse.builder()
                .id(execution.getId())
                .jobName(execution.getJobName())
                .triggerType(execution.getTriggerType())
                .triggeredBy(execution.getTriggeredBy())
                .status(execution.getStatus())
                .startedAt(execution.getStartedAt())
                .finishedAt(execution.getFinishedAt())
                .processedCount(execution.getProcessedCount())
                .successCount(execution.getSuccessCount())
                .failedCount(execution.getFailedCount())
                .summary(execution.getSummary())
                .errorMessage(execution.getErrorMessage())
                .build();
    }
}
