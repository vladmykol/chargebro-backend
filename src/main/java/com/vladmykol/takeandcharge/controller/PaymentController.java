package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.service.PaymentService;
import com.vladmykol.takeandcharge.service.RentService;
import com.vladmykol.takeandcharge.service.UserWalletService;
import com.vladmykol.takeandcharge.service.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@RestController
@RequestMapping(EndpointConst.API_PAY)
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserWalletService userWalletService;
    private final RentService rentService;
    private final WebSocketServer webSocketServer;


    @GetMapping(API_PAY_CHECKOUT)
    public String getCheckoutUrl() {
        return paymentService.getCheckoutUrlWithTokenForCardAuth();
    }

    @PostMapping(API_PAY_CALLBACK_AUTH)
    @ResponseStatus(HttpStatus.OK)
    public void authCallback(@RequestBody FondyResponse callbackDto) {
        try {
            paymentService.processCallback(callbackDto);

            userWalletService.saveCard(callbackDto);
        } catch (Exception e) {
            webSocketServer.sendPaymentErrorMessage("Added card is not valid");
        }
    }

    @PostMapping(API_PAY_CALLBACK_HOLD)
    @ResponseStatus(HttpStatus.OK)
    public void rentPaymentCallback(@RequestBody FondyResponse callbackDto) {
        rentService.rentUpdateWithPaymentCallback(callbackDto);
    }

}
