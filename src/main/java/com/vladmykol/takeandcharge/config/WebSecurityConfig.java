package com.vladmykol.takeandcharge.config;

import com.vladmykol.takeandcharge.security.JwtAuthorizationFilter;
import com.vladmykol.takeandcharge.security.TokenService;
import com.vladmykol.takeandcharge.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenService tokenService;

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .addFilter(new JwtAuthorizationFilter(tokenService, authenticationManager()))
//                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
//                .antMatchers("/rent/location").permitAll()
//                not authorized user can access this links
                .antMatchers(API_AUTH + API_AUTH_LOGIN).permitAll()
                .antMatchers(API_AUTH + API_AUTH_REGISTER_INIT).permitAll()
                .antMatchers(API_AUTH + API_AUTH_REGISTER).permitAll()
//                payment callback is authorized by signature from liqpay
                .antMatchers(API_PAY + API_PAY_CALLBACK).permitAll()
//                call back for SMS Gateway
                .antMatchers(API_SMS + API_SMS_CALLBACK).permitAll()
//                manual authorization for socket clients
                .antMatchers(API_SOCKET_RENT).permitAll()
                .anyRequest().authenticated()
                .and().httpBasic()
                .and().logout().logoutUrl(API_AUTH + API_AUTH_LOGOUT).permitAll();
    }
}
