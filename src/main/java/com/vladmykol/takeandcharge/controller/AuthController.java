package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.*;
import com.vladmykol.takeandcharge.exceptions.UserAlreadyExist;
import com.vladmykol.takeandcharge.exceptions.UserIsBlocked;
import com.vladmykol.takeandcharge.exceptions.UserIsFrozen;
import com.vladmykol.takeandcharge.security.TokenService;
import com.vladmykol.takeandcharge.service.RegisterUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@RestController
@RequestMapping(EndpointConst.API_AUTH)
@RequiredArgsConstructor
public class AuthController {


    private final RegisterUserService registerUserService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;


    @PostMapping(API_AUTH_LOGIN)
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return generateAuthResponse(userDetails.getId());
    }

    private AuthenticationResponse generateAuthResponse(String userId) {
        String generateJwtToken = tokenService.generateAuthToken(userId);

        return new AuthenticationResponse(generateJwtToken);
    }

    @PostMapping(API_AUTH_REGISTER_INIT)
    public SmsRegistrationTokenInfo preRegisterStep(@RequestParam String phone, HttpServletResponse response) throws IOException {
        try {
            var validationCode = registerUserService.preSingUp(phone);
            return tokenService.generateSmsToken(validationCode);

        } catch (UserIsFrozen e) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "User is frozen for some time");
        } catch (UserIsBlocked e) {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "User is blocked");
        } catch (UserAlreadyExist e) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "User already exists");
        }
        return null;
    }

    @PostMapping(API_AUTH_REGISTER)
    public AuthenticationResponse singUp(@Valid @RequestBody SingUpDto singUpDto, HttpServletResponse response) throws IOException {
        var smsCode = tokenService.parseSmsToken(singUpDto.getToken());

        if (smsCode.equals(singUpDto.getSmsCode())) {
            var userId = registerUserService.saveUser(singUpDto);
            return generateAuthResponse(userId);
        } else {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Invalid SMS code");
        }

        return null;
    }


}
