package com.carshop.service;

import com.carshop.dto.request.CreateTimeSlotRequest;
import com.carshop.dto.response.TimeSlotResponse;
import com.carshop.entity.BookingStatus;
import com.carshop.entity.TimeSlot;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.TimeSlotMapper;
import com.carshop.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotMapper timeSlotMapper;

    @Transactional
    public TimeSlotResponse createTimeSlot(CreateTimeSlotRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        if (timeSlotRepository.existsByDateAndStartTime(request.getDate(), request.getStartTime())) {
            throw new DuplicateResourceException("A time slot already exists for this date and start time");
        }
        TimeSlot slot = TimeSlot.builder()
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isAvailable(true)
                .technicianName(request.getTechnicianName())
                .build();
        return timeSlotMapper.toResponse(timeSlotRepository.save(slot));
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getTimeSlotsByDate(LocalDate date) {
        return timeSlotRepository.findAllByDate(date)
                .stream().map(timeSlotMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TimeSlotResponse updateTimeSlot(Long id, CreateTimeSlotRequest request) {
        TimeSlot slot = findById(id);
        if (timeSlotRepository.existsActiveBookingForTimeSlot(id, BookingStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot modify a time slot that has an active booking");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        slot.setDate(request.getDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setTechnicianName(request.getTechnicianName());
        return timeSlotMapper.toResponse(timeSlotRepository.save(slot));
    }

    @Transactional
    public void deleteTimeSlot(Long id) {
        TimeSlot slot = findById(id);
        if (timeSlotRepository.existsActiveBookingForTimeSlot(id, BookingStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot delete a time slot that has an active booking");
        }
        timeSlotRepository.delete(slot);
    }

    private TimeSlot findById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
    }
}
