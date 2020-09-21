package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.conts.PaymentType;
import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.entity.Rent;
import com.vladmykol.takeandcharge.exceptions.PaymentGatewayException;
import com.vladmykol.takeandcharge.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private static final int MINIMAL_AMOUNT = 100;
    private final PaymentRepository paymentRepository;
    private final UserWalletService userWalletService;
    private final FondyService paymentGateway;

    public List<Payment> getAllPaymentHistory() {
        return paymentRepository.findAll();
    }

    public String getCheckoutUrlWithTokenForCardAuth() {
        final var newPayment = paymentRepository.save(
                Payment.builder()
                        .type(PaymentType.CARD_AUTH)
                        .orderId(UUID.randomUUID().toString())
                        .amount(MINIMAL_AMOUNT)
                        .build()
        );

        String url;
        try {
            url = paymentGateway.getCheckoutUrlWithTokenForCardAuth(newPayment);
            newPayment.setSuccesses(true);
        } finally {
            paymentRepository.save(newPayment);
        }

        return url;
    }


    public String holdMoney(String rentId) {
        // TODO: 9/15/2020 get valid card from wallet
        final var validPaymentMethodsOrdered = userWalletService.getValidPaymentMethodsOrdered();

        String paymentId = "";
        final var iterator = validPaymentMethodsOrdered.iterator();
        while (iterator.hasNext()) {
            final var userWallet = iterator.next();
            final var newPayment = paymentRepository.save(
                    Payment.builder()
                            .type(PaymentType.HOLD)
                            .orderId(rentId)
                            .amount(getHoldAmount())
                            .build()
            );

            try {
                paymentGateway.holdMoneyByToken(userWallet.getCardToken(), newPayment);
                newPayment.setSuccesses(true);
                final var savedPayment = paymentRepository.save(newPayment);

                paymentId = savedPayment.getId();
                break;
            } catch (PaymentGatewayException e) {
                if (!iterator.hasNext()) {
                    throw e;
                }
            } finally {
                paymentRepository.save(newPayment);
            }
        }

        if (paymentId.isEmpty()) {
            throw new RuntimeException("Please add at least one valid credit card");
        }

        return paymentId;
    }


    public void captureMoneyForRent(Rent rent) {
        var holdPayment = getPaymentById(rent.getHoldMoneyPaymentId());

        final var newPayment = paymentRepository.save(
                Payment.builder()
                        .type(PaymentType.CAPTURE_HOLD)
                        .orderId(holdPayment.getOrderId())
                        .amount(rent.getPrice())
                        .build()
        );

        try {
            paymentGateway.captureMoney(newPayment);
            newPayment.setSuccesses(true);
        } finally {
            paymentRepository.save(newPayment);
        }
    }

    public void reverseMoney(Rent rent) {
        var holdPayment = getPaymentById(rent.getHoldMoneyPaymentId());

        final var newPayment = Payment.builder()
                .type(PaymentType.REVERSE)
                .orderId(holdPayment.getOrderId())
                .amount(holdPayment.getAmount())
                .build();

        try {
            paymentGateway.reverseMoney(newPayment);
            newPayment.setSuccesses(true);
        } finally {
            paymentRepository.save(newPayment);
        }
    }


    public Payment processCallback(FondyResponse callbackDto) {
        final var existingPayment = paymentRepository.findById(callbackDto.getMerchant_data());
        Payment payment;

        if (existingPayment.isEmpty()) {
            payment = Payment.builder()
                    .type(PaymentType.UNEXPECTED_CALLBACK)
                    .orderId(callbackDto.getOrder_id())
                    .amount(callbackDto.getAmount())
                    .callback(callbackDto)
                    .build();
        } else {
            payment = existingPayment.get();
            payment.setCallback(callbackDto);
            payment.setSuccesses(false);
        }

        try {
            paymentGateway.validateResponse(callbackDto, true);
            payment.setSuccesses(true);
        } finally {
            paymentRepository.save(payment);
        }

        return payment;
    }

    public void calcRentPrice(Rent rent) {
//       < 1min
        if (rent.getRentTime() < 60000) {
            rent.setPrice(0);
//           < 5min
        } else if (rent.getRentTime() < 300000) {
            rent.setPrice(100);
        } else {
            rent.setPrice(200);
        }
    }

    int getHoldAmount() {
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
