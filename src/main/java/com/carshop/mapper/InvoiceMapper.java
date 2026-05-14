package com.carshop.mapper;

import com.carshop.dto.response.InvoiceResponse;
import com.carshop.entity.Invoice;
import com.carshop.entity.InvoiceItem;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .bookingId(invoice.getBooking().getId())
                .bookingReference(invoice.getBooking().getBookingReference())
                .customerId(invoice.getCustomer().getId())
                .customerName(invoice.getCustomer().getName())
                .customerPhoneNumber(invoice.getCustomer().getPhoneNumber())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .subtotalAmount(invoice.getSubtotalAmount())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .outstandingAmount(invoice.getOutstandingAmount())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .items(invoice.getItems().stream().map(this::toItemResponse).toList())
                .build();
    }

    private InvoiceResponse.InvoiceItemResponse toItemResponse(InvoiceItem item) {
        return InvoiceResponse.InvoiceItemResponse.builder()
                .id(item.getId())
                .serviceId(item.getServiceId())
                .serviceName(item.getServiceName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
