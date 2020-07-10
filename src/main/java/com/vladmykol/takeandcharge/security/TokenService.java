package com.vladmykol.takeandcharge.security;

import com.vladmykol.takeandcharge.dto.SmsRegistrationTokenInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
@Slf4j
public class TokenService {
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_WITH_TOKEN = "Authorization";

    @Value("${take-and-charge.api.auth.token-secret}")
    private String authSecret;

    @Value("${take-and-charge.api.auth.token-expiration}")
    private int authExpirationMs;

    @Value("${take-and-charge.api.sms.token-secret}")
    private String smsSecret;

    @Value("${take-and-charge.api.sms.token-expiration.min}")
    private int smsExpirationMin;

    public String generateAuthToken(String userId) {
        return Jwts
                .builder()
                .setId("AuthJwt")
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + authExpirationMs))
                .signWith(SignatureAlgorithm.HS512,
                        authSecret.getBytes()).compact();
    }

    public SmsRegistrationTokenInfo generateSmsToken(String code) {
        var smsToken = Jwts
                .builder()
                .setId("SmSJwt")
                .setSubject(code)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + smsExpirationMin * 60 * 1000))
                .signWith(SignatureAlgorithm.HS512,
                        smsSecret.getBytes()).compact();

        return new SmsRegistrationTokenInfo(smsExpirationMin, code, smsToken);
    }

    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_WITH_TOKEN);
        if (!StringUtils.startsWithIgnoreCase(token, TOKEN_PREFIX)) return null;
        else return StringUtils.replace(token, TOKEN_PREFIX, "");
    }

    public String parseAuthToken(String token) {
        return Jwts.parser().setSigningKey(authSecret.getBytes()).parseClaimsJws(token).getBody().getSubject();
    }

    public String parseSmsToken(String token) {
        return Jwts.parser().setSigningKey(smsSecret.getBytes()).parseClaimsJws(token).getBody().getSubject();
    }

}
