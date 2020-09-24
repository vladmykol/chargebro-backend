package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.conts.PaymentType;
import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.exceptions.PaymentException;
import com.vladmykol.takeandcharge.repository.PaymentRepository;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private static final int MINIMAL_AMOUNT = 100;
    private final PaymentRepository paymentRepository;
    private final UserWalletService userWalletService;
    private final FondyService paymentGateway;

    public int getRentPriceAmount(long rentTimeMs) {
//       < 5min
        if (rentTimeMs < 300000) {
            return 0;
//           < 5min
        } else {
            final var trueRentTime = rentTimeMs - 300000;
            final var min = (int) Math.ceil((double) trueRentTime / 60000);
            return min * 100;
        }
    }

    public List<Payment> getAllPaymentHistory() {
        return paymentRepository.findAll();
    }

    public String getCheckoutUrlWithTokenForCardAuth() {
        final var newPayment = paymentRepository.save(
                Payment.builder()
                        .type(PaymentType.CARD_AUTH)
                        .amount(MINIMAL_AMOUNT)
                        .build()
        );

        String url;
        try {
            url = paymentGateway.getCheckoutUrlWithTokenForCardAuth(newPayment);
        } finally {
            paymentRepository.save(newPayment);
        }

        return url;
    }

    public String holdMoney(String rentId, boolean isDeposit, int amount) {
        // TODO: 9/15/2020 get valid card from wallet
        final var validPaymentMethodsOrdered = userWalletService.getValidPaymentMethodsOrdered();

        if (validPaymentMethodsOrdered == null || validPaymentMethodsOrdered.isEmpty()) {
            throw new PaymentException("Please add at least one valid credit card");
        }

        String paymentId = "";
//        final var iterator = validPaymentMethodsOrdered.iterator();
//        while (iterator.hasNext()) {
//            final var userWallet = iterator.next();
        final var userWallet = validPaymentMethodsOrdered.get(0);
        final var newPayment = paymentRepository.save(
                Payment.builder()
                        .type(isDeposit ? PaymentType.DEPOSIT : PaymentType.CHARGE)
                        .rentId(rentId)
                        .amount(amount)
                        .build()
        );

        try {
            paymentGateway.holdMoneyByToken(userWallet.getCardToken(), newPayment);
            paymentId = newPayment.getId();

//            break;
//        } catch (PaymentGatewayException e) {
//            if (!iterator.hasNext()) {
//                throw e;
//            }
        } finally {
            paymentRepository.save(newPayment);
        }
//        }

        return paymentId;
    }

    public void reversePayment(String paymentId) {
        var holdPayment = getPaymentById(paymentId);

        try {
            paymentGateway.reverseMoney(holdPayment);
        } finally {
            paymentRepository.save(holdPayment);
        }
    }

    public Payment processCallback(FondyResponse callbackDto) {
        final var existingPayment = paymentRepository.findById(callbackDto.getOrder_id());
        Payment payment;

        if (existingPayment.isEmpty()) {
            payment = Payment.builder()
                    .type(PaymentType.UNEXPECTED_CALLBACK)
                    .amount(callbackDto.getAmount())
                    .orderStatus(callbackDto.getOrder_status())
                    .callbacks(Collections.singletonList(callbackDto))
                    .build();
        } else {
            payment = existingPayment.get();
            SecurityUtil.setUser(payment.getUserId());

            payment.setOrderStatus(callbackDto.getOrder_status());
            payment.getCallbacks().add(callbackDto);
        }

        try {
            paymentGateway.validateResponse(callbackDto, true);
        } finally {
            paymentRepository.save(payment);
        }

        return payment;
    }

    public int getHoldAmount() {
        return 200;
    }

    private Payment getPaymentById(String id) {
        final var optionalPayment = paymentRepository.findById(id);

        if (optionalPayment.isEmpty()) {
            throw new RuntimeException("Payment with ID " + id + " is not found");
        }
        return optionalPayment.get();
    }
}
