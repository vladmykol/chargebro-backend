package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.conts.PowerBankStatus;
import com.vladmykol.takeandcharge.entity.PowerBank;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PowerBankRepository extends MongoRepository<PowerBank, String> {
    List<PowerBank> findByUserIdAndStatus(String userId, PowerBankStatus status);
}
