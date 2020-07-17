package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;


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

    @NotBlank
    @Size(max = 20)
    private String card_token;

    @CreatedDate
    @NonNull
    private Date createAt;

    @Version
    private Long version;

    @LastModifiedDate
    private Date lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

    @DBRef
    private LiqPayHistory liqPayHistory;
}
