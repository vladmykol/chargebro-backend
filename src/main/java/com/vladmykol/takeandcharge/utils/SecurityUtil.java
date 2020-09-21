package com.vladmykol.takeandcharge.utils;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class SecurityUtil {

    public static String getUser() {
        var userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userId == null) {
            throw new UsernameNotFoundException("User id is empty");
        }
        return userId;
    }

    public static void setUser(String userId) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}

