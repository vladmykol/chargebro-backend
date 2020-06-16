package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.CustomUserDetails;
import com.vladmykol.takeandcharge.dto.UserDto;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return CustomUserDetails.build(user);
    }

    public void registerUser(UserDto userDto) {
        User user = User.builder()
                .userName(userDto.getFirstName())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .build();
        userRepository.save(user);
    }
}
