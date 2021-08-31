package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import com.vladmykol.takeandcharge.conts.PowerBankStatus;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;


@Data
@Document
public class PowerBank {
    @Id
    private String id;

    private String currentRentId;

    @CreatedDate
    @NonNull
    private Date firstAddedAt;

    @LastModifiedDate
    private Date lastOperationDate;

    @LastModifiedBy
    private String userId;

    @Indexed
    @NotNull
    private PowerBankStatus status = PowerBankStatus.RETURNED;
}
