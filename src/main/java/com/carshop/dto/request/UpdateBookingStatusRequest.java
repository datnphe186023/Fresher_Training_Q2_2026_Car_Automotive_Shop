package com.carshop.dto.request;

import com.carshop.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingStatusRequest {

    @NotNull(message = "Status is required")
    private BookingStatus status;
}
