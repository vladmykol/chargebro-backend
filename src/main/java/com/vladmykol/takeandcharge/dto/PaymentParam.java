package com.vladmykol.takeandcharge.dto;

import com.vladmykol.takeandcharge.entity.Payment;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentParam {
    private Payment payment;
    private int amount;
    private String cardToken;
}
