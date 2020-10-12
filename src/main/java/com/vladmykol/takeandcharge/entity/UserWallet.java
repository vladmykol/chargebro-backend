package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Objects;


@Data
@Document
@Builder
public class UserWallet {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    @CreatedBy
    @NonNull
    @Indexed
    private String userId;

    private String paymentId;

    @NonNull
    @Indexed
    private String cardToken;

    @NonNull
    private String cardType;

    @NonNull
    private String maskedCard;

    @CreatedDate
    @NonNull
    private Date createAt;

    @Version
    private Long version;

    @LastModifiedDate
    private Date lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

    private boolean isRemoved;

    public Boolean getIsRemoved() {
        return Objects.requireNonNullElse(isRemoved, false);
    }
}
