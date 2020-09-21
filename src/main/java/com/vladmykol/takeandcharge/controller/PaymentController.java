package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.exceptions.PaymentGatewayException;
import com.vladmykol.takeandcharge.service.PaymentService;
import com.vladmykol.takeandcharge.service.RentService;
import com.vladmykol.takeandcharge.service.UserWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@RestController
@RequestMapping(EndpointConst.API_PAY)
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService payService;
    private final UserWalletService userWalletService;
    private final RentService rentService;

    @GetMapping(API_PAY_CHECKOUT)
    public String getCheckoutUrl() {
        return payService.getCheckoutUrlWithTokenForCardAuth();
    }

    @PostMapping(API_PAY_CALLBACK_AUTH)
    @ResponseStatus(HttpStatus.OK)
    public void authCallback(@RequestBody FondyResponse callbackDto) {
        try {
            final var payment = payService.processCallback(callbackDto);
            userWalletService.saveCard(payment.getId(), callbackDto);
        } catch (PaymentGatewayException ignore) {
        }

    }


    @PostMapping(API_PAY_CALLBACK_HOLD)
    @ResponseStatus(HttpStatus.OK)
    public void rentPaymentCallback(@RequestBody FondyResponse callbackDto) {
        rentService.rentUpdateWithPaymentCallback(callbackDto);

    }

}
