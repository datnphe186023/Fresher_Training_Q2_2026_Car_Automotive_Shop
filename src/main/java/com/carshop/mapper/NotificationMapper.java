package com.carshop.mapper;

import com.carshop.dto.response.NotificationResponse;
import com.carshop.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .type(notification.getType())
                .status(notification.getStatus())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .errorMessage(notification.getErrorMessage())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
