package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import com.vladmykol.takeandcharge.conts.PaymentType;
import com.vladmykol.takeandcharge.dto.FondyRequest;
import com.vladmykol.takeandcharge.dto.FondyResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
@Builder
public class Payment {
    @Id
    private String id;
    @Indexed
    private String orderId;

    private int amount;

    private PaymentType type;

    private boolean isSuccesses;

    private FondyRequest request;

    private FondyResponse response;

    private FondyResponse callback;

    @CreatedBy
    @Indexed
    @NonNull
    private String userId;

    @CreatedDate
    @NonNull
    private Date createAt;

}