package com.vladmykol.takeandcharge.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liqpay.LiqPay;
import com.liqpay.LiqPayUtil;
import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.entity.LiqPayHistory;
import com.vladmykol.takeandcharge.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_PAY_CALLBACK;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_PAY_CHECKOUT;

@RestController
@RequestMapping(EndpointConst.API_PAY)
@RequiredArgsConstructor
public class PaymentController {
    private final String PRIVATE_KEY = "sandbox_eEPZJtlITBq1CZP8k6SrzFd6WtDVFS3rt5fCiVOM";
    private final String PUBLIC_KEY = "sandbox_i85026641584";
    private final PaymentService paymentService;
    @Value("${take-and-charge.api.domain}")
    private String domainName;
    @Value("${server.port}")
    private String serverPort;
    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;

    @GetMapping(API_PAY_CHECKOUT)
    public String sendPayment(Principal principal) {
        String baseAddress = "http" + (sslEnabled ? "s" : "") + "://" + domainName + ":" + serverPort;

        Map<String, String> params = new HashMap();
        params.put("action", "auth");
        params.put("amount", "1");
        params.put("currency", "UAH");
        params.put("result_url", baseAddress);
        params.put("description", "Authorization");
        params.put("customer", principal.getName());
        params.put("language", "ua");
        params.put("recurringbytoken", "1");
        params.put("server_url", baseAddress + "/pay/callback");
        params.put("sandbox", "1"); // enable the testing environment and card will NOT charged. If not set will be used property isCnbSandbox()
        LiqPay liqpay = new LiqPay(PUBLIC_KEY, PRIVATE_KEY);
        String html = liqpay.cnb_form(params);
        var data = StringUtils.substringBetween(html, "name=\"data\" value=\"", "\" />");
        var signature = StringUtils.substringBetween(html, "name=\"signature\" value=\"", "\" />");

        String str = String.format("https://www.liqpay.ua/api/3/checkout?data=%s&signature=%s",
                data, signature);

        return str;
    }

    @PostMapping(API_PAY_CALLBACK)
    @ResponseStatus(HttpStatus.OK)
    public void callback(@RequestParam String data, @RequestParam String signature) throws JsonProcessingException {
        var sign = LiqPayUtil.base64_encode(LiqPayUtil.sha1(PRIVATE_KEY +
                data +
                PRIVATE_KEY));
        if (!sign.equals(signature)) {
            throw new BadCredentialsException("request signature is invalid");
        }

        var jsonData = new String(new Base64().decode(data));
        LiqPayHistory liqPayCallback;
        try {
            liqPayCallback = new ObjectMapper().readValue(jsonData, LiqPayHistory.class);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(liqPayCallback.getCustomer(), null);
            SecurityContextHolder.getContext().setAuthentication(auth);

            paymentService.savePaymentCallback(liqPayCallback);
        } catch (IOException e) {
            // TODO: 7/17/2020 reaction on parsing error
            throw e;
        }
    }

}
