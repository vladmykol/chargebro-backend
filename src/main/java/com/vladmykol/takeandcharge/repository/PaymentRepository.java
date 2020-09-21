package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}
