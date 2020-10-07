package com.vladmykol.takeandcharge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;


@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FondyResponse {
    private String order_id;
    private String error_message;
    private Integer error_code;
    private String capture_status;
    private Integer merchant_id;
    private Integer amount;
    private String currency;
    private String order_status;
    private String response_status;
    private String signature;
    private String tran_type;
    private String sender_cell_phone;
    private String sender_account;
    private String masked_card;
    private Integer card_bin;
    private String card_type;
    private String rrn;
    private String approval_code;
    private Integer response_code;
    private String response_description;
    private Integer reversal_amount;
    private Integer settlement_amount;
    private String settlement_currency;
    private String order_time;
    private String settlement_date;
    private Integer eci;
    private Integer fee;
    private String payment_system;
    private String sender_email;
    private Integer payment_id;
    private String actual_amount;
    private String actual_currency;
    private String product_id;
    private String merchant_data;
    private String verification_status;
    private String rectoken;
    private String rectoken_lifetime;
    private String additional_info;
    private String checkout_url;

    @ToString.Include(name = "rectoken")
    private String sensitiveFieldMasker1() {
        return rectoken == null ? null : "*****";
    }

    @ToString.Include(name = "signature")
    private String sensitiveFieldMasker2() {
        return signature == null ? null : "*****";
    }
}
