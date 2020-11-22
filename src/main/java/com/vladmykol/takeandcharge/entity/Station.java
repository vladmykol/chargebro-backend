package com.vladmykol.takeandcharge.entity;

import lombok.Data;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Document
public class Station {
    private String id;

    @Indexed(unique = true)
    @NotNull
    private String shortId;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @NotNull
    private Point location;

    private Integer maxCapacity;

    private String placeName;

    private String address;

    private String mapUrl;

    private Integer simPhoneNumber;

    private Date lastLogIn;
}
