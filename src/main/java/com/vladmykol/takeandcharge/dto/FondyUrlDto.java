package com.vladmykol.takeandcharge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FondyUrlDto {
    private String response_status;
    private String error_message;
    private Integer error_code;
    private String checkout_url;
    private Integer payment_id;
}
