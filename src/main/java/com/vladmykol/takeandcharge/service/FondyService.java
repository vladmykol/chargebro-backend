package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.conts.PaymentType;
import com.vladmykol.takeandcharge.dto.FondyCallbackRespDto;
import com.vladmykol.takeandcharge.dto.FondyRequest;
import com.vladmykol.takeandcharge.dto.FondyRequestWrapper;
import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.exceptions.PaymentGatewayException;
import com.vladmykol.takeandcharge.exceptions.PaymentGatewaySignatureException;
import com.vladmykol.takeandcharge.utils.FondyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FondyService {
    private static final String CHECKOUT_URI = "/checkout/url";
    private static final String WITH_TOKEN_URI = "/recurring";
    private static final String CAPTURE_URI = "/capture/order_id";
    private static final String REVERSE_URI = "/reverse/order_id";
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${takeandcharge.pay.link}")
    private String paymentApiUrl;
    @Value("${takeandcharge.pay.pass}")
    private String privateKey;
    @Value("${takeandcharge.pay.user}")
    private String publicKey;
    @Value("${takeandcharge.api.domain}")
    private String callbackUrl;

    String getCheckoutUrlWithTokenForCardAuth(Payment payment) {
        var request = FondyRequest.builder()
                .amount(payment.getAmount())
                .currency("UAH")
                .order_id(payment.getId())
                .verification("Y")
                .required_rectoken("Y")
                .order_desc("Verify user payment method")
//        params.put("lang", "uk");
                .server_callback_url(callbackUrl + API_PAY + API_PAY_CALLBACK_AUTH)
                .build();

        payment.setRequest(request);
        var response = postForResponse(request, CHECKOUT_URI);
        payment.setResponse(response);

        validateResponse(response, false);

        return response.getCheckout_url();
    }


    void holdMoneyByToken(String token, Payment payment) {
        final var request = FondyRequest.builder()
                .amount(payment.getAmount())
                .currency("UAH")
                .order_id(payment.getId())
                .order_desc("Charge for renting a power bank")
                //        params.put("lang", "uk");
                .server_callback_url(callbackUrl + API_PAY + API_PAY_CALLBACK_HOLD)
                .merchant_data(payment.getId())
                .rectoken(token)
                .build();

        if (payment.getType() == PaymentType.DEPOSIT) {
            request.setPreauth("Y");
            request.setOrder_desc("Deposit before rent");
        }
        payment.setRequest(request);
        final var response = postForResponse(request, WITH_TOKEN_URI);
        payment.setResponse(response);

        validateResponse(response, true);
    }

    void captureMoney(Payment payment) {
        final var request = FondyRequest.builder()
                .amount(payment.getAmount())
                .currency("UAH")
                .order_id(payment.getId())
                //        params.put("lang", "uk");
                .build();

        payment.setRequest(request);
        final var response = postForResponse(request, CAPTURE_URI);
        payment.setResponse(response);

        validateResponse(response, false);
    }

    void reverseMoney(Payment payment) {
        final var request = FondyRequest.builder()
                .amount(payment.getAmount())
                .currency("UAH")
                .order_id(payment.getId())
                .comment("Returning a deposit as user returned a powerbank")
                .build();

        final var response = postForResponse(request, REVERSE_URI);

        validateResponse(response, false);
    }


    private FondyResponse postForResponse(FondyRequest body, String uri) {
        body.setMerchant_id(publicKey);
        body.setSignature(FondyUtil.generateSignature(body, privateKey));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<FondyRequestWrapper> request =
                new HttpEntity<>(new FondyRequestWrapper(body), headers);

        var response = restTemplate.postForObject(paymentApiUrl + uri, request, FondyCallbackRespDto.class);
        log.debug("Calling payment API:\nrequest: {}\nresponse: {}", request, response);

        if (response == null) {
            throw new RuntimeException("Empty response from " + uri);
        }

        return response.getResponse();
    }


    public void validateResponse(FondyResponse response, boolean isSignaturePresent) {
        validateForErrors(response);
        if (isSignaturePresent) {
            validateForSignature(response);
        }
    }


    public void validateForErrors(FondyResponse response) {
        final var errorMessage = extractErrorMessage(response);
        if (StringUtils.isNotBlank(errorMessage)) {
            throw new PaymentGatewayException(errorMessage);
        }
    }


    private void validateForSignature(FondyResponse callbackDto) {
        if (!isValidSignature(callbackDto)) {
            throw new PaymentGatewaySignatureException("Not valid signature for order id: " + callbackDto.getOrder_id());
        }
    }

    private String extractErrorMessage(FondyResponse response) {
        if (!"success".equalsIgnoreCase(response.getResponse_status())) {
            var message = response.getError_message();
            if (StringUtils.isNotBlank(response.getResponse_description())) {
                message = message + " " + response.getResponse_description();
            }
            return message;
        }
        return null;
    }


    public boolean isValidSignature(FondyResponse callbackDto) {
        return FondyUtil.generateSignature(callbackDto, privateKey).equals(callbackDto.getSignature());
    }

}
