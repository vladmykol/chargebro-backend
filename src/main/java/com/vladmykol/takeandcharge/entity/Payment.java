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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document
@Builder
public class Payment {
    @Id
    private String id;

    @NonNull
    private String rentId;

    private int amount;

    private PaymentType type;

    private String orderStatus;

    private FondyRequest request;

    private FondyResponse response;

    @Builder.Default
    private List<FondyResponse> callbacks = new ArrayList<>();

    @CreatedBy
    @Indexed
    @NonNull
    private String userId;

    @CreatedDate
    @NonNull
    private Date createAt;

}