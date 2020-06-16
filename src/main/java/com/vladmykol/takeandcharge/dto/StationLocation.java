package com.vladmykol.takeandcharge.dto;

import lombok.Data;
import org.springframework.data.geo.Point;


@Data
public class StationLocation {
    private String id;

    private Point location;
}
