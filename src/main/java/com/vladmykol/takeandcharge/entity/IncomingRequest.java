package com.vladmykol.takeandcharge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mongodb.lang.NonNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpHeaders;

import java.util.Date;

@Data
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncomingRequest {
    @Id
    @JsonIgnore
    private String id;

    @CreatedDate
    @NonNull
    private Date createDate;

    private String method;

    private String ipAddress;

    @Indexed
    private String request;

    private String requestParams;

    private HttpHeaders requestHeaders;

    private Integer responseStatus;

}
