package com.carshop.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyResponse {
    private String customerName;
    private String phoneNumber;
    private Integer loyaltyPoints;
}
