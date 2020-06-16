package com.vladmykol.takeandcharge.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;


@Data
@Document
@Builder
public class ReconnectionLog {
    @EqualsAndHashCode.Exclude
    @Id
    private String id;

    private String stationId;

    @CreatedDate
    private Date connectedAt;

    private Date disconnectedAt;
}
