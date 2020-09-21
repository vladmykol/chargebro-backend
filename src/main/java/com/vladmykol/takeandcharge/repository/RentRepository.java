package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.Rent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RentRepository extends MongoRepository<Rent, String> {
    Optional<Rent> findByPowerBankIdAndReturnedAtIsNullAndIsActiveRentTrue(String powerBankId);

    List<Rent> findByUserIdAndIsActiveRentTrue(String userId);

    List<Rent> findByUserId(String userId);
}
