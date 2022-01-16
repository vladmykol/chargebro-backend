package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.IncomingRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequestLogRepository extends MongoRepository<IncomingRequest, String>, RequestLogRepositoryCustom {
    boolean existsByRequest(String request);
}
