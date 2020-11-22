package com.vladmykol.takeandcharge.dto;

import lombok.Data;


@Data
public class StationInfoDto {
    private String id;
    private String shortId;
    private double locationX;
    private double locationY;
    private String placeName;
    private String address;
    private String workingHours = "8:30 - 21:00";
    private String mapUrl;
    private int maxCapacity;
    private Integer simPhoneNumber;
}
