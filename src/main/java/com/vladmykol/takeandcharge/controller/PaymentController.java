package com.vladmykol.takeandcharge.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liqpay.LiqPay;
import com.liqpay.LiqPayUtil;
import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.CustomUserDetails;
import com.vladmykol.takeandcharge.entity.LiqPayHistory;
import com.vladmykol.takeandcharge.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_PAY_CALLBACK;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_PAY_CHECKOUT;

@RestController
@RequestMapping(EndpointConst.API_PAY)
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @Value("${takeandcharge.pay.link}")
    private String checkoutLink;
    @Value("${takeandcharge.pay.pass}")
    private String privateKey;
    @Value("${takeandcharge.pay.user}")
    private String publicKey;
    @Value("${takeandcharge.api.domain}")
    private String callbackUri;

    @GetMapping(API_PAY_CHECKOUT)
    public String sendPayment(@AuthenticationPrincipal CustomUserDetails activeUser) {

        Map<String, String> params = new HashMap<>();
        params.put("action", "auth");
        params.put("amount", "1");
        params.put("currency", "UAH");
        params.put("result_url", callbackUri);
        params.put("description", "Authorization");
        params.put("customer", activeUser.getUsername());
        params.put("language", "ua");
        params.put("recurringbytoken", "1");
        params.put("server_url", callbackUri + "/pay/callback");
        params.put("sandbox", "1"); // enable the testing environment and card will NOT charged. If not set will be used property isCnbSandbox()
        LiqPay liqpay = new LiqPay(publicKey, privateKey);
        String html = liqpay.cnb_form(params);
        var data = StringUtils.substringBetween(html, "name=\"data\" value=\"", "\" />");
        var signature = StringUtils.substringBetween(html, "name=\"signature\" value=\"", "\" />");

        return String.format(checkoutLink, data, signature);
    }

    @PostMapping(API_PAY_CALLBACK)
    @ResponseStatus(HttpStatus.OK)
    public void callback(@RequestParam String data, @RequestParam String signature) throws JsonProcessingException {
        var sign = LiqPayUtil.base64_encode(LiqPayUtil.sha1(privateKey +
                data +
                privateKey));
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
            liqPayCallback = new LiqPayHistory();
            liqPayCallback.setStatus("Fail to parse data");
            liqPayCallback.setAction(jsonData);
            paymentService.savePaymentCallback(liqPayCallback);
            throw e;
        }
    }

}
