package com.carshop.dto.response;

import com.carshop.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private Long bookingId;
    private String bookingReference;
    private Long customerId;
    private String customerName;
    private String customerPhoneNumber;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private InvoiceStatus status;
    private String notes;
    private List<InvoiceItemResponse> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceItemResponse {
        private Long id;
        private Long serviceId;
        private String serviceName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
