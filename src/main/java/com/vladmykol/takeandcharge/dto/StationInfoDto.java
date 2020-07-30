package com.vladmykol.takeandcharge.dto;

import lombok.Data;


@Data
public class StationInfoDto {
    private String id;
    private double locationX;
    private double locationY;
    private String placeName;
    private String address;
    private String mapUrl;
    private int maxCapacity;
}
