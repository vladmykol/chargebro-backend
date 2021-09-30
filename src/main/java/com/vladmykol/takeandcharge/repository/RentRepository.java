package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.conts.RentStage;
import com.vladmykol.takeandcharge.entity.Rent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RentRepository extends MongoRepository<Rent, String> {
    List<Rent> findByStageIn(List<RentStage> stages);
    List<Rent> findByUserIdAndStageIn(String userId, List<RentStage> stages);

    List<Rent> findByUserIdAndPowerBankReturnedAtNotNull(String userId);
}
