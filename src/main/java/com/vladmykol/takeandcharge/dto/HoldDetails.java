package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class HoldDetails {
    private String rentId;
    private boolean isPreAuth;
    private int amount;
    private String rentTimeFormatted;
    private String powerBankId;
    private String userPhone;
}
