package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.RentHistory;
import com.vladmykol.takeandcharge.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RentHistoryRepository extends MongoRepository<RentHistory, String> {
    Optional<RentHistory> findByPowerBankIdAndReturnedAtIsNull(String powerBankId);
    List<RentHistory> findByUserIdAndReturnedAtIsNull(String userId);
    List<RentHistory> findByUserId(String userId);
}
