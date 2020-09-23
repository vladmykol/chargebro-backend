package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import com.vladmykol.takeandcharge.conts.RentStage;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Data
@Document
@Builder
public class Rent {
    @Id
    private String id;

    @NonNull
    private String takenInStationId;

    private String returnedToStationId;

    private String powerBankId;

    private short powerBankSlot;

    @Indexed
    private boolean isActiveRent;

    private RentStage stage;

    private String depositPaymentId;

    private String chargePaymentId;

    private int price;

    @CreatedBy
    @NonNull
    private String userId;

    @CreatedDate
    @NonNull
    private Date createDate;

    private Date takenAt;

    private Date returnedAt;

    @Version
    private Long version;

    @LastModifiedDate
    private Date lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

    private String errorCause;

    public void markRentStart(String powerBankId) {
        this.powerBankId = powerBankId;
        this.isActiveRent = true;
        this.takenAt = new Date();
    }

    public void markPbReturned(String stationId) {
        this.returnedToStationId = stationId;
        this.returnedAt = new Date();
    }

    public void markRentFinished() {
        this.isActiveRent = false;
        this.stage = RentStage.SUCCESSFULLY_FINISHED;
    }

    public long getRentTime() {
        long returnedAt = getReturnedAt() == null ? System.currentTimeMillis() : getReturnedAt().getTime();
        return Math.abs(returnedAt - getTakenAt().getTime());
    }
}
