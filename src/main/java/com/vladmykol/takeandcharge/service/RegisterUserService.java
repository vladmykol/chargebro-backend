package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.conts.RoleEnum;
import com.vladmykol.takeandcharge.dto.SingUpDto;
import com.vladmykol.takeandcharge.dto.SmsRegistrationTokenInfo;
import com.vladmykol.takeandcharge.entity.Role;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.exceptions.SmsSendingError;
import com.vladmykol.takeandcharge.exceptions.UserAlreadyExist;
import com.vladmykol.takeandcharge.repository.RoleRepository;
import com.vladmykol.takeandcharge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.vladmykol.takeandcharge.entity.User.UserStatus.*;

@RequiredArgsConstructor
@Service
public class RegisterUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    @Value("${takeandcharge.api.sms.token-expiration.min}")
    private int smsExpirationMin;

    @Value("${takeandcharge.api.sms.throttling.min}")
    private int throttlingMin;


    private User getUser(String username) {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    }

    public SmsRegistrationTokenInfo initUserUpdate(String userName, boolean isPassReset) {
        var regToken = new SmsRegistrationTokenInfo();
        Optional<User> userByUserName = userRepository.findByUserName(userName);

        User user;
        if (userByUserName.isPresent()) {
            user = userByUserName.get();

            switch (user.getUserStatus()) {
                case INITIALIZED:
                    if (user.getSmsId() != null) {
                        if (smsService.checkIfSmsSend(user.getSmsId())) {
                            if (ifLastRequestExpired(user.getPasswordDate())) {
                                sendChangePasswordSms(user, MAX_REGISTER_ATTEMPS_REACHED);
                            } else {
                                regToken.setWarningMessage("SMS is on the way. Please wait couple more minutes");
                            }
                        } else {
                            sendChangePasswordSms(user, RE_INITIALIZED_VIBER);
                            regToken.setWarningMessage("Please check Viber for PIN code");
                        }
                    } else {
                        throw new SmsSendingError();
                    }
                    break;
                case RE_INITIALIZED_VIBER:
                    if (user.getSmsId() != null) {
                        if (smsService.checkIfSmsSend(user.getSmsId())) {
                            if (ifLastRequestExpired(user.getPasswordDate())) {
                                throw new SmsSendingError("This number is blocked for too many requests");
                            } else {
                                regToken.setWarningMessage("Message is on the way. Please check Viber");
                            }
                        } else {
                            throw new SmsSendingError();
                        }
                    } else {
                        throw new SmsSendingError();
                    }
                    break;
                case MAX_REGISTER_ATTEMPS_REACHED:
                    if (ifRequestWasLongTimeAgo(user.getPasswordDate())) {
                        sendChangePasswordSms(user, INITIALIZED);
                    } else {
                        throw new SmsSendingError("This number is blocked for too many requests");
                    }
                    break;
                case REGISTERED:
                    if (isPassReset) {
                        sendChangePasswordSms(user, MAX_REGISTER_ATTEMPS_REACHED);
                    } else {
                        throw new UserAlreadyExist();
                    }
                    break;
            }
        } else {
            Role userRole = roleRepository.findByRole(RoleEnum.USER);
            user = User.builder()
                    .userName(userName)
                    .roles(Collections.singleton(userRole))
                    .build();

            sendChangePasswordSms(user, INITIALIZED);
        }

        userRepository.save(user);

        regToken.setCode(user.getRegisterCode());
        return regToken;
    }

    private String generateRegisterCode() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    private void sendChangePasswordSms(User user, User.UserStatus status) {
        user.setPasswordDate(new Date());
        user.setRegisterCode(generateRegisterCode());
        user.setUserStatus(status);

        try {
            var smsId = smsService.sendVerificationCode(user.getRegisterCode(), user.getUserName());
            user.setSmsId(smsId);
        } catch (Exception e) {
            userRepository.save(user);
            throw e;
        }
    }

    private boolean ifLastRequestExpired(Date lastRequestDate) {
        if (lastRequestDate == null) return true;
        long diff = new Date().getTime() - lastRequestDate.getTime();
        return diff > smsExpirationMin * 60 * 1000;
    }

    private boolean ifRequestWasLongTimeAgo(Date lastRequestDate) {
        if (lastRequestDate == null) return true;
        long diff = new Date().getTime() - lastRequestDate.getTime();
        return diff > throttlingMin * 60 * 1000;
    }

    public User saveUser(SingUpDto userDto) {
        User existingUser = getUser(userDto.getName());

        existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        existingUser.setPasswordDate(new Date());
        existingUser.setBonusAmount(0);
        existingUser.setUserStatus(REGISTERED);

        return userRepository.save(existingUser);
    }

    public void saveUser(User user) {
        user.getRoles().forEach(role -> {
            var roleDocument = roleRepository.findByRole(role.getRole());
            role.setId(roleDocument.getId());
        });
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }


    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
