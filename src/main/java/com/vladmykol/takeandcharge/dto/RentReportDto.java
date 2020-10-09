package com.vladmykol.takeandcharge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vladmykol.takeandcharge.conts.RentStage;
import lombok.Builder;
import lombok.Data;

import java.util.Date;


@Data
@Builder
public class RentReportDto {
    private String orderId;

    private String takePlace;

    private String takeAddress;

    private String returnPlace;

    private String returnAddress;

    private boolean isActiveRent;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastModifiedDate;

    private String powerBankId;

    private int price;

    private String depositPaymentId;

    private String chargePaymentId;

    private String userPhone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date takenAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date returnedAt;

    private RentStage stage;

    private Integer lastErrorCode;

    private String lastErrorMessage;
}
