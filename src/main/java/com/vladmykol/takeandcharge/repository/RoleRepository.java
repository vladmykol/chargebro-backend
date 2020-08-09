package com.vladmykol.takeandcharge.repository;

import com.vladmykol.takeandcharge.conts.RoleEnum;
import com.vladmykol.takeandcharge.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {
    Role findByRole(RoleEnum role);
}
