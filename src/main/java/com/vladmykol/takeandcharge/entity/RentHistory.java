package com.vladmykol.takeandcharge.entity;

import com.mongodb.lang.NonNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;


@Data
@Document
@Builder
public class RentHistory {
    @Id
    private String id;

    @NotBlank
    @Indexed
    private String powerBankId;

    @CreatedBy
    @NonNull
    private String userId;

    @CreatedDate
    @NonNull
    private Date rentAt;

    private Date returnedAt;

    @Version
    private Long version;

    @LastModifiedDate
    private Date lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

}
