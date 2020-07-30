package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.Station;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface StationRepository extends MongoRepository<Station, String> {
//    @Query(value = "{}", fields = "{_id: 1, location: 1, maxCapacity: 1}")
    List<Station> findByLocationNear(Point location, Distance distance);
}
