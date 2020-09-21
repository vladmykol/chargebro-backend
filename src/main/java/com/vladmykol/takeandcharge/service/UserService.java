package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public String getUserPhone(String userId) {
        final var userOptional = userRepository.findById(userId);
        return userOptional.map(User::getUserName).orElse(null);

    }
}
