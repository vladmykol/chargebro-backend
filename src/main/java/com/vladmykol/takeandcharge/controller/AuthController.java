package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.CustomUserDetails;
import com.vladmykol.takeandcharge.dto.UserDto;
import com.vladmykol.takeandcharge.dto.AuthenticationResponse;
import com.vladmykol.takeandcharge.dto.LoginRequest;
import com.vladmykol.takeandcharge.cabinet.StationListener;
import com.vladmykol.takeandcharge.security.TokenService;
import com.vladmykol.takeandcharge.service.CustomUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_AUTH_LOGIN;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_AUTH_SINGUP;

@RestController
@RequestMapping(EndpointConst.API_AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    private final CustomUserService customUserService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final StationListener stationListener;


    @PostMapping(API_AUTH_LOGIN)
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String generateJwtToken = tokenService.generateJwtToken(userDetails.getId());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken(generateJwtToken);

        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping(API_AUTH_SINGUP)
    public void authenticateUser(@Valid @RequestBody UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        customUserService.registerUser(userDto);
    }
}
