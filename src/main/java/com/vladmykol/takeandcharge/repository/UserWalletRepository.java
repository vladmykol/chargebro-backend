package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.UserWallet;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserWalletRepository extends MongoRepository<UserWallet, String> {
}
