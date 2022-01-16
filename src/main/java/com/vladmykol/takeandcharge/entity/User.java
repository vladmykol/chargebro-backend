package com.vladmykol.takeandcharge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;


@Data
@Document
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    @NotBlank
    @Size(max = 20)
    @Indexed(unique = true)
    private String userName;

    @Size(max = 20)
    private String firstName;

    @Size(max = 20)
    private String lastName;

    @Size(max = 50)
    @Email
    private String email;

    @DBRef
    private Set<Role> roles;

    @CreatedDate
    private Date createDate;

    @LastModifiedDate
    private Date lastModifiedDate;

    @NotBlank
    @Size(max = 120)
    private String password;

    private Date passwordDate;

    private String smsId;

    private String registerCode;

    private UserStatus userStatus;

    private Integer bonusAmount;

    public enum UserStatus {
        INITIALIZED,
        RE_INITIALIZED,
        RE_INITIALIZED_VIBER,
        REGISTERED,
        MAX_REGISTER_ATTEMPS_REACHED
    }
}
