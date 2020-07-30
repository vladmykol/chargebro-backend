package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.StationInfoDto;
import com.vladmykol.takeandcharge.entity.Station;
import com.vladmykol.takeandcharge.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {
    private final StationRepository stationRepository;
    private final ModelMapper stationInfoMapper;

    public List<StationInfoDto> findStationsNearBy(double x, double y) {
        Distance distance = new Distance(100, Metrics.KILOMETERS);
        Point point = new Point(x, y);
        var nearByStations = stationRepository.findByLocationNear(point, distance);
        return nearByStations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private StationInfoDto convertToDto(Station station) {
        return stationInfoMapper.map(station, StationInfoDto.class);
    }

    private Station convertToEntity(StationInfoDto stationInfoDto) {
        var station = stationInfoMapper.map(stationInfoDto, Station.class);
        var point = new Point(stationInfoDto.getLocationX(), stationInfoDto.getLocationY());
        station.setLocation(point);
        return station;
    }

    public void update(StationInfoDto stationInfoDto) {
        var station = convertToEntity(stationInfoDto);
        stationRepository.save(station);
    }

    public List<StationInfoDto> findAll() {
        var allStations = stationRepository.findAll();
        return allStations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
