package com.vladmykol.takeandcharge.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ReceiveSmsDto {
    private String secret;
    private String message_id;
    private String sql_sms_id;
    private String status;
    private Date sent_date;
    private Date dlr_date;
    private String error;
}
