package com.carshop.dto.response;

import com.carshop.entity.PaymentMethod;
import com.carshop.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String referenceCode;
    private LocalDateTime paymentDate;
    private String notes;
}
