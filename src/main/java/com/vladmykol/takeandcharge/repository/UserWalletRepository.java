package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.entity.UserWallet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserWalletRepository extends MongoRepository<UserWallet, String> {
    List<UserWallet> findByUserIdOrderByLastModifiedDateDesc(String userId);
}
