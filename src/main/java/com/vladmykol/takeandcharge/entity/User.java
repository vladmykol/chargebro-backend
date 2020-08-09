package com.vladmykol.takeandcharge.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
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

    @NotBlank
    @Size(max = 20)
    private String firstName;

    @NotBlank
    @Size(max = 20)
    private String lastName;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @DBRef
    private Set<Role> roles;

    @CreatedDate
    private Date createDate;
//
//    @Version
//    private Long version;
//
    @LastModifiedDate
    private Date lastModifiedDate;

    @NotBlank
    @Size(max = 120)
    private String password;

    private Date passwordDate;

    private String smsId;

    private String registerCode;

    private UserStatus userStatus;

    public enum UserStatus {
        INITIALIZED,
        RE_INITIALIZED,
        RE_INITIALIZED_VIBER,
        REGISTERED,
        MAX_REGISTER_ATTEMPS_REACHED
    }
}
