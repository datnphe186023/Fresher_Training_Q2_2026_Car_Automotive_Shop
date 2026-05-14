package com.carshop.mapper;

import com.carshop.dto.response.TimeSlotResponse;
import com.carshop.entity.TimeSlot;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {

    public TimeSlotResponse toResponse(TimeSlot slot) {
        if (slot == null) return null;
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .date(slot.getDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.isAvailable())
                .technicianName(slot.getTechnicianName())
                .build();
    }
}
