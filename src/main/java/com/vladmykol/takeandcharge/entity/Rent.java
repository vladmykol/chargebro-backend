package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import com.vladmykol.takeandcharge.conts.RentStage;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

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

    @Indexed
    private String powerBankId;

    private short powerBankSlot;

    @Indexed
    private RentStage stage;

    private String depositPaymentId;

    private String chargePaymentId;

    private int price;

    @CreatedBy
    @NonNull
    @Indexed
    private String userId;

    @CreatedDate
    @NonNull
    private Date createDate;

    private Date powerBankTakenAt;

    private Date powerBankReturnedAt;

    @Version
    private Long version;

    @LastModifiedDate
    private Date lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

    @Getter(AccessLevel.NONE)
    private RentError lastError;

    private String comment;

    public void setReturnedTo(String stationId) {
        this.returnedToStationId = stationId;
        this.powerBankReturnedAt = new Date();
    }

    public void markRentFinished() {
        this.stage = RentStage.SUCCESSFULLY_FINISHED;
    }

    public long getRentTime() {
        long returnedAt = getPowerBankReturnedAt() == null ? System.currentTimeMillis() : getPowerBankReturnedAt().getTime();
        return Math.abs(returnedAt - getPowerBankTakenAt().getTime());
    }

    public int getLastErrorCodeValue() {
        if (getLastErrorCode() != null) {
            return getLastErrorCode().value();
        } else {
            return 0;
        }
    }

    public HttpStatus getLastErrorCode() {
        if (lastError != null) {
            return lastError.getStatus();
        } else {
            return null;
        }
    }

    public String getLastErrorMessage() {
        if (lastError != null) {
            return lastError.getMessage();
        } else {
            return null;
        }
    }

    public void setPowerBankTaken() {
        this.powerBankTakenAt = new Date();
        this.stage = RentStage.POWERBANK_TAKEN;
    }

    public void setPowerBankUnlocked() {
        this.stage = RentStage.UNLOCK_POWERBANK;
    }
}
