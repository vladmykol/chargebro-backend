package com.vladmykol.takeandcharge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liqpay.LiqPay;
import com.liqpay.LiqPayUtil;
import com.vladmykol.takeandcharge.entity.LiqPayHistory;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.entity.UserWallet;
import com.vladmykol.takeandcharge.repository.LiqPayHistoryRepository;
import com.vladmykol.takeandcharge.repository.UserRepository;
import com.vladmykol.takeandcharge.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiqPayService {
    private final LiqPayHistoryRepository liqPayHistoryRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserRepository userRepository;
    @Value("${takeandcharge.pay.link}")
    private String checkoutLink;
    @Value("${takeandcharge.pay.pass}")
    private String privateKey;
    @Value("${takeandcharge.pay.user}")
    private String publicKey;
    @Value("${takeandcharge.api.domain}")
    private String callbackUri;

    public void savePaymentCallback(LiqPayHistory liqPayHistory) {
        var savedLiqPayHistory = liqPayHistoryRepository.save(liqPayHistory);

        if ("auth".equalsIgnoreCase(savedLiqPayHistory.getAction())) {
            Optional<User> user = userRepository.findById(savedLiqPayHistory.getCustomer());
            if (user.isPresent()) {
                final var cardExists = userWalletRepository.existsByCardToken(savedLiqPayHistory.getCard_token());
                if (!cardExists) {
                    var userWallet = UserWallet.builder()
                            .userId(user.get().getId())
                            .cardToken(savedLiqPayHistory.getCard_token())
//                        .liqPayHistory(savedLiqPayHistory)
                            .build();
                    userWalletRepository.save(userWallet);
                }
            }
        }
    }

    public List<LiqPayHistory> getAllPaymentHistory() {
        return liqPayHistoryRepository.findAll();
    }

    public String prepareCheckoutUrl(String username) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "auth");
        params.put("amount", "1");
        params.put("currency", "UAH");
        params.put("result_url", callbackUri);
        params.put("description", "Authorization");
        params.put("customer", username);
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

    public void callback(String data, String signature) throws JsonProcessingException {
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

            savePaymentCallback(liqPayCallback);
        } catch (IOException e) {
            liqPayCallback = new LiqPayHistory();
            liqPayCallback.setStatus("Fail to parse data");
            liqPayCallback.setAction(jsonData);
            savePaymentCallback(liqPayCallback);
            throw e;
        }
    }
}
