package com.carshop.mapper;

import com.carshop.dto.response.PaymentResponse;
import com.carshop.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .referenceCode(payment.getReferenceCode())
                .paymentDate(payment.getPaymentDate())
                .notes(payment.getNotes())
                .build();
    }
}
