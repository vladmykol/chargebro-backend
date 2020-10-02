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

    //    125 = 1.25
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

    public String prepareCheckoutUrlWithTokenForCardAuth() {
        final var newPayment = paymentRepository.save(
                Payment.builder()
                        .type(PaymentType.CARD_AUTH)
                        .amount(MINIMAL_AMOUNT)
                        .build()
        );
        final var response = paymentGateway.prepareCheckoutUrl(newPayment);
        validateAndSaveResponseNoSignature(newPayment, response);

        checkForErrors(newPayment);

        return response.getCheckout_url();
    }


    public Payment holdMoney(String rentId, boolean isDeposit, int amount) {
        // TODO: 9/15/2020 get valid card from wallet
        final var validPaymentMethodsOrdered = userWalletService.getValidPaymentMethodsOrdered();

        if (validPaymentMethodsOrdered == null || validPaymentMethodsOrdered.isEmpty()) {
            throw new PaymentException("Please add at least one valid credit card");
        }

        final var userWallet = validPaymentMethodsOrdered.get(0);
        final var newPayment = paymentRepository.save(
                Payment.builder()
                        .type(isDeposit ? PaymentType.DEPOSIT : PaymentType.CHARGE)
                        .rentId(rentId)
                        .amount(amount)
                        .build()
        );

        final var response = paymentGateway.holdMoneyByToken(userWallet.getCardToken(), newPayment);
        validateAndSavePayment(newPayment, response);

        return newPayment;
    }

    public Payment reversePayment(String paymentId) {
        final var optionalPayment = paymentRepository.findById(paymentId);

        if (optionalPayment.isEmpty()) {
            throw new RuntimeException("Payment for reverse with ID " + paymentId + " is not found");
        }

        final var response = paymentGateway.reverseMoney(optionalPayment.get());
        validateAndSavePayment(optionalPayment.get(), response);

        return optionalPayment.get();
    }

    public Payment savePaymentCallback(FondyResponse callbackDto) {
        final var existingPayment = paymentRepository.findById(callbackDto.getOrder_id());
        Payment payment;

        if (existingPayment.isEmpty()) {
            payment = Payment.builder()
                    .type(PaymentType.UNEXPECTED_CALLBACK)
                    .amount(callbackDto.getAmount())
                    .orderStatus(callbackDto.getOrder_status())
                    .responses(Collections.singletonList(callbackDto))
                    .build();
        } else {
            payment = existingPayment.get();
            SecurityUtil.setUser(payment.getUserId());

            payment.setOrderStatus(callbackDto.getOrder_status());
        }

        validateAndSavePayment(payment, callbackDto);

        return payment;
    }

    public int getHoldAmount() {
        return 200;
    }

    public void checkForErrors(Payment payment) {
        if (payment.getErrorMessage() != null) {
            throw new PaymentException(payment.getErrorMessage());
        }
    }


    private void validateAndSavePayment(Payment payment, FondyResponse response, boolean isSignaturePresent) {
        payment.getResponses().add(response);
        payment.setErrorMessage(paymentGateway.validateResponse(response, isSignaturePresent));
        paymentRepository.save(payment);
    }

    private void validateAndSavePayment(Payment payment, FondyResponse response) {
        validateAndSavePayment(payment, response, true);
    }

    private void validateAndSaveResponseNoSignature(Payment payment, FondyResponse response) {
        validateAndSavePayment(payment, response, false);
    }

}
