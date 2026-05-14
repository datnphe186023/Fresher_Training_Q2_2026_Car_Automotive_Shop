package com.carshop.dto.response;

import com.carshop.entity.InvoiceStatus;
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
public class InvoiceSummaryResponse {
    private Long id;
    private String invoiceNumber;
    private String bookingReference;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private InvoiceStatus status;
    private LocalDateTime issueDate;
}
