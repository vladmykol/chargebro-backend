package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.*;
import com.vladmykol.takeandcharge.security.JwtProvider;
import com.vladmykol.takeandcharge.service.RegisterUserService;
import com.vladmykol.takeandcharge.service.UserWalletService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping(EndpointConst.API_AUTH)
@RequiredArgsConstructor
@Deprecated(since = "app version 1.2")
public class AuthController {
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final RegisterUserService registerUserService;
    private final UserWalletService userWalletService;

    @PostMapping("/login")
    public UserInfoDto login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        final var userHasPaymentMethod = userWalletService.isUserHasPaymentMethod(userDetails.getUsername());

        return UserInfoDto.builder()
                .token(generateAuthResponse(userDetails.getUsername()).getToken())
                .isHasCard(userHasPaymentMethod ? 1 : 0)
                .phone(userDetails.getPhone())
                .build();
    }

    @PostMapping("/init")
    public SmsRegistrationTokenInfo preRegisterStep(@RequestParam String phone) {
        SmsRegistrationTokenInfo result = registerUserService.initUserUpdate(phone, false);
        jwtProvider.generateSmsToken(result);

        return result;
    }

    @PostMapping("/reset")
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
