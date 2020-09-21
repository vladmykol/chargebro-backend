package com.vladmykol.takeandcharge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FondyRequest {
    private String order_id;
    private String verification;
    private String required_rectoken;
    private Integer amount;
    private String order_desc;
    private String currency;
    private String server_callback_url;
    private String merchant_data;
    private String merchant_id;
    private String signature;
    private String preauth;
    private String rectoken;
    private String comment;
}
