package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.service.RegisterUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(EndpointConst.API_USERS)
@RequiredArgsConstructor
public class UserController {
    private final RegisterUserService registerUserService;

    @PostMapping()
    public void saveUser(@Valid @RequestBody User user) {
        registerUserService.saveUser(user);
    }

    @GetMapping()
    public List<User> findAll() {
        return registerUserService.findAll();
    }
}
