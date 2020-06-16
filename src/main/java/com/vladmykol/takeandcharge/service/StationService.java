package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.StationLocation;
import com.vladmykol.takeandcharge.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationService {
    private final StationRepository stationRepository;

    public List<StationLocation> findStationsNearBy(double x, double y) {
        Distance distance = new Distance(5, Metrics.KILOMETERS);
        Point point = new Point(x, y);
        return stationRepository.findByLocationNear(point, distance);
    }
}
