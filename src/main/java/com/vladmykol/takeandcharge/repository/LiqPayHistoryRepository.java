package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.LiqPayHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LiqPayHistoryRepository extends MongoRepository<LiqPayHistory, String> {
}
