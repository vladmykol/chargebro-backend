package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.SingUpDto;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.exceptions.UserAlreadyExist;
import com.vladmykol.takeandcharge.exceptions.UserIsBlocked;
import com.vladmykol.takeandcharge.exceptions.UserIsFrozen;
import com.vladmykol.takeandcharge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class RegisterUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    @Value("${takeandcharge.api.sms.token-expiration.min}")
    private int smsExpirationMin;


    private User getUser(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }

    public String preSingUp(String userName) {
        Optional<User> existingUser = userRepository.findByUserName(userName);

        if (existingUser.isPresent()) {
            switch (existingUser.get().getUserStatus()) {
                case INITIALIZED:
                    existingUser.get().setPasswordDate(new Date());
                    existingUser.get().setUserStatus(User.userStatus.RE_INITIALIZED);
                    break;
                case RE_INITIALIZED:
                    if (ifLastRequestExpired(existingUser.get().getPasswordDate())) {
                        existingUser.get().setPasswordDate(new Date());
                    } else {
                        throw new UserIsFrozen(10);
                    }
                    break;
                case BLOCKED:
                    throw new UserIsBlocked();
                case REGISTERED:
                    throw new UserAlreadyExist();
            }
            userRepository.save(existingUser.get());
        } else {
            User newUser = User.builder()
                    .userName(userName)
                    .userStatus(User.userStatus.INITIALIZED)
                    .passwordDate(new Date())
                    .build();

            userRepository.save(newUser);
        }

        var validationCode = String.format("%04d", new Random().nextInt(10000));
        smsService.sendValidationSms(validationCode, userName);

        return validationCode;
    }

    private boolean ifLastRequestExpired(Date lastRequestDate) {
        long diff = new Date().getTime() - lastRequestDate.getTime();
        return diff > smsExpirationMin * 60 * 1000;
    }

    public String saveUser(SingUpDto userDto) {
        User existingUser = getUser(userDto.getName());

        existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        existingUser.setPasswordDate(new Date());
        existingUser.setUserStatus(User.userStatus.REGISTERED);

        return userRepository.save(existingUser).getId();
    }

}
