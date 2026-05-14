package com.carshop.dto.response;

import com.carshop.entity.NotificationStatus;
import com.carshop.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String recipient;
    private String subject;
    private String content;
    private NotificationType type;
    private NotificationStatus status;
    private String relatedEntityType;
    private Long relatedEntityId;
    private String errorMessage;
    private LocalDateTime createdAt;
}
