package com.vladmykol.takeandcharge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmykol.takeandcharge.entity.Role;
import com.vladmykol.takeandcharge.entity.User;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode
@ToString
public class CustomUserDetails implements UserDetails {

    private final List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
        for (Role role : user.getRoles()) {
            var authority = new SimpleGrantedAuthority("ROLE_" + role.getRole().name());
            simpleGrantedAuthorities.add(authority);
        }
    }

    @Override
    public String getUsername() {
        return user.getId();
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return simpleGrantedAuthorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getPhone() {
        return user.getUserName();
    }
}
