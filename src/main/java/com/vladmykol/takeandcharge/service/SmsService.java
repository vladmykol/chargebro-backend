package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.SendSmsBodyDto;
import com.vladmykol.takeandcharge.dto.SendSmsRequestDto;
import com.vladmykol.takeandcharge.dto.SendSmsResponseDto;
import com.vladmykol.takeandcharge.exceptions.SmsSendingError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    @Value("${sms.gateway.uri}")
    private String gatewayUri;

    @Value("${sms.gateway.send}")
    private String sendCommand;

    @Value("${sms.gateway.token}")
    private String authToken;

    public void sendValidationSms(String validationCode, String phone) {
        var sendSmsBodyDto = SendSmsBodyDto.builder().text("Take&Charge code: " + validationCode).build();

        var requestBody = SendSmsRequestDto.builder()
                .recipients(Collections.singletonList(phone))
                .sms(sendSmsBodyDto)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + authToken);

        HttpEntity<SendSmsRequestDto> request = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        SendSmsResponseDto responseDto = restTemplate.postForObject(gatewayUri + sendCommand, request, SendSmsResponseDto.class);

        if ((responseDto != null ? responseDto.getResponse_code() : 0) != 801) {
            log.error("Sms was not send: {}", responseDto);
            throw new SmsSendingError("Cannot send validation SMS");
        }
    }
}
