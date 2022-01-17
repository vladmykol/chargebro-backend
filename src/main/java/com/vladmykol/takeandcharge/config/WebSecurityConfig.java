package com.vladmykol.takeandcharge.config;

import com.vladmykol.takeandcharge.conts.RoleEnum;
import com.vladmykol.takeandcharge.repository.RequestLogRepository;
import com.vladmykol.takeandcharge.security.JwtAuthorizationFilter;
import com.vladmykol.takeandcharge.security.JwtProvider;
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
    private final JwtProvider jwtProvider;
    private final RequestLogRepository requestLogRepository;

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
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtProvider, customUserDetailsService, requestLogRepository))
//                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
//                .antMatchers("/rent/location").permitAll()
//                not authorized user can access this links
                .antMatchers(
                        //   auth operations is open
                        API_AUTH + "/**",
                        API_VERSION_1 + API_AUTH + "/**",
                        //   anyone can fetch stations locations
                        API_STATIONS + API_STATIONS_NEARBY,
                        //   redirect to mobile version is available for all
                        API_APP + "/**",
//                payment callbacks are authorized by signature
                        API_PAY + API_PAY_CALLBACK + "/**",
//                call back for SMS Gateway not secured
                        API_SMS + API_SMS_CALLBACK,
//                socket clients (stations) are not authorized yet
                        API_SOCKET_RENT).permitAll()
                .antMatchers("/actuator/**").hasRole(RoleEnum.ADMIN.name())
                .antMatchers("/swagger-ui/**").hasRole(RoleEnum.ADMIN.name())
                .antMatchers("/swgr/**").hasRole(RoleEnum.ADMIN.name())
                .antMatchers(API_ADMIN + "/**").hasRole(RoleEnum.ADMIN.name())
                .anyRequest().authenticated()
                .and().httpBasic()
                .and().logout().logoutUrl(API_AUTH + API_AUTH_LOGOUT).permitAll();
    }
}
