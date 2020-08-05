package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.SendSmsBodyDto;
import com.vladmykol.takeandcharge.dto.SendSmsRequestDto;
import com.vladmykol.takeandcharge.dto.SendSmsResponseDto;
import com.vladmykol.takeandcharge.dto.SmsStatusRequestDto;
import com.vladmykol.takeandcharge.exceptions.SmsSendingError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    @Value("${takeandcharge.sms.gateway.uri}")
    private String gatewayUri;

    @Value("${takeandcharge.sms.gateway.send}")
    private String sendCommand;

    @Value("${takeandcharge.sms.gateway.status}")
    private String statusCommand;

    @Value("${takeandcharge.sms.gateway.token}")
    private String authToken;

    public String sendValidationSms(String validationCode, String phone, boolean isViber) {
        var requestBody = SendSmsRequestDto.builder()
                .recipients(Collections.singletonList(phone))
                .build();

        var body = new SendSmsBodyDto(validationCode);
        if (isViber) {
            requestBody.setViber(body);
        } else {
            requestBody.setSms(body);
        }

        HttpEntity<SendSmsRequestDto> request = new HttpEntity<>(requestBody, getHttpHeaders());
        SendSmsResponseDto response = postForObject(request, sendCommand, SendSmsResponseDto.class);

        validateSendSmsResponse(response);

        return getMessageId(response);
    }

    public boolean checkIfSmsSend(String messageId) {
        var invalidStatuses = Arrays.asList("Rejected", "Failed", "Cancelled");

        var requestBody = new SmsStatusRequestDto(messageId);

        HttpEntity<SmsStatusRequestDto> request = new HttpEntity<>(requestBody, getHttpHeaders());
        var responseDto = postForObject(request, statusCommand, SendSmsResponseDto.class);

        if (responseDto.getResponse_result().isEmpty()) {
            return false;
        } else {
            return (!invalidStatuses.contains(responseDto.getResponse_result().get(0).getStatus()));
        }
    }


    private <T> T postForObject(HttpEntity<?> request, String destination, Class<T> responseType) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(gatewayUri + destination, request, responseType);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + authToken);
        return headers;
    }

    private void validateSendSmsResponse(SendSmsResponseDto response) {
        if (response == null || response.getResponse_code() != 801) {
            log.error("Sms was not send: {}", response);
            throw new SmsSendingError();
        }
    }

    private String getMessageId(SendSmsResponseDto response) {
        return response.getResponse_result().get(0).getMessage_id();
    }
}
