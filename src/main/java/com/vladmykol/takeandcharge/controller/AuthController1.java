package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.AuthenticationResponse;
import com.vladmykol.takeandcharge.dto.SingUpDto;
import com.vladmykol.takeandcharge.dto.SmsRegistrationTokenInfo;
import com.vladmykol.takeandcharge.dto.UserInfoDto;
import com.vladmykol.takeandcharge.security.JwtProvider;
import com.vladmykol.takeandcharge.service.RegisterUserService;
import com.vladmykol.takeandcharge.service.UserWalletService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping(EndpointConst.API_VERSION_1 + EndpointConst.API_AUTH)
@RequiredArgsConstructor
public class AuthController1 {
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final RegisterUserService registerUserService;
    private final UserWalletService userWalletService;

    @PostMapping("/init")
    public SmsRegistrationTokenInfo resetUserPass(@RequestParam String phone) {
        SmsRegistrationTokenInfo result = registerUserService.initUserUpdate(phone, true);
        jwtProvider.generateSmsToken(result);

        return result;
    }

    @PostMapping("/register")
    public UserInfoDto singUp(@Valid @RequestBody SingUpDto singUpDto) {
        var smsCode = "";
        try {
            smsCode = jwtProvider.parseSmsToken(singUpDto.getToken());
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Invalid SMS code");
        }

        if (smsCode.equals(singUpDto.getSmsCode())) {
            singUpDto.setPassword("empty");
            var user = registerUserService.saveUser(singUpDto);
            final var authenticationResponse = generateAuthResponse(user.getId());
            final var userHasPaymentMethod = userWalletService.isUserHasPaymentMethod(user.getId());

            return UserInfoDto.builder()
                    .token(authenticationResponse.getToken())
                    .isHasCard(userHasPaymentMethod ? 1 : 0)
                    .phone(user.getUserName())
                    .build();

        } else {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Invalid SMS code");
        }
    }

    private AuthenticationResponse generateAuthResponse(String userId) {
        String generateJwtToken = jwtProvider.generateAuthToken(userId);

        return new AuthenticationResponse(generateJwtToken);
    }

}
