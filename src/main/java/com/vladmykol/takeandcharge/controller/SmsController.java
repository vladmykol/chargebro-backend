package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.dto.ReceiveSmsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_SMS;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_SMS_CALLBACK;

@RestController
@RequestMapping(API_SMS)
@RequiredArgsConstructor
public class SmsController {

    @PostMapping(API_SMS_CALLBACK)
    public void statusOfSmsDelivery(@Valid @RequestBody ReceiveSmsDto receiveSmsDto) {
        System.out.println(receiveSmsDto);
    }

}
