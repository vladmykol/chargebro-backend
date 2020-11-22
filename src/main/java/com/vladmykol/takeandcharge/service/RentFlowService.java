package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.conts.PaymentType;
import com.vladmykol.takeandcharge.conts.RentStage;
import com.vladmykol.takeandcharge.dto.HoldDetails;
import com.vladmykol.takeandcharge.dto.RentConfirmationDto;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.entity.Rent;
import com.vladmykol.takeandcharge.entity.RentError;
import com.vladmykol.takeandcharge.exceptions.HttpException;
import com.vladmykol.takeandcharge.exceptions.PaymentException;
import com.vladmykol.takeandcharge.exceptions.RentAlreadyInProgress;
import com.vladmykol.takeandcharge.repository.RentRepository;
import com.vladmykol.takeandcharge.utils.ExceptionUtil;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentFlowService {
    private final StationService stationService;
    private final PaymentService paymentService;
    private final RentRepository rentRepository;
    private final RentService rentService;
    private final WebSocketServer webSocketServer;
    private final UserService userService;
    private final UserWalletService userWalletService;
    private final PowerBankService powerBankService;

    public RentConfirmationDto getBeforeRentInfo(String stationId) {
//        final var powerBankInfo = stationService.findMaxChargedPowerBank(stationId);
        if (!userWalletService.isUserHasPaymentMethod(SecurityUtil.getUser())) {
            throw PaymentException.noValidPaymentMethod();
        }

        final var holdAmount = paymentService.getHoldAmount() / 100;
        final var userBonus = userService.getUserBonus(SecurityUtil.getUser());

        return RentConfirmationDto.builder()
                .stationId(stationId)
                .holdAmount(holdAmount + ".00")
                // TODO: 9/17/2020 calc bonus
                .bonusAmount(String.valueOf(userBonus))
                .build();
    }

    public void syncRentStart(String stationId) {
        if (rentService.isUserHasActiveRent()) {
            throw new RentAlreadyInProgress();
        }

        Rent rent = rentRepository.save(
                Rent.builder()
                        .stage(RentStage.CHECK)
                        .takenInStationId(stationId)
                        .build()
        );

        executeRentStep(() -> {
            checkAvailablePowerBanks(rent);
            holdMoneyBeforeRent(rent);

        }, rent, true);
    }


    public void updateRentWithPayment(Payment payment) {
        Optional<Rent> optionalRent = rentRepository.findById(payment.getRentId());

        if (optionalRent.isEmpty()) {
            log.error("Rent with id {} is not found", payment.getRentId());
            return;
        }

        executeRentStep(() -> {

            paymentService.checkForErrors(payment);
            webSocketServer.sendMoneyHoldConfirmationMessage(payment.getOrderStatus());
            processRentPayment(payment.getType(), payment.getOrderStatus(), optionalRent.get());

        }, optionalRent.get(), false);
    }

    //    @Async(AsyncConfiguration.RETURN_POWER_BANK_TASK_EXECUTOR)
    public void returnPowerBankAction(String rentId, String stationId) {
        Optional<Rent> optionalRent = rentRepository.findById(rentId);

        optionalRent.ifPresent(rent -> {
            SecurityUtil.setUser(rent.getUserId());
            rent.setReturnedTo(stationId);

            executeRentStep(() -> {
                finalizeRent(rent);
            }, rent, false);
        });
    }

    private void finalizeRent(Rent rent) {
        final var rentPriceAmount = paymentService.getRentPriceAmount(rent.getRentTime());
        rent.setPrice(rentPriceAmount);

        if (rentPriceAmount > 0) {
            chargeMoneyAfterRent(rent);
        } else {
            finishRent(rent);
        }
    }

    private void processRentPayment(PaymentType paymentType, String status, Rent rent) {
        if (isWaitingForDepositConfirmation(paymentType, status, rent.getStage())) {
            givePowerBank(rent);
        } else if (isWaitingForChargeConfirmation(paymentType, status, rent.getStage())) {
            finishRent(rent);
        }
    }

    private boolean isWaitingForDepositConfirmation(PaymentType paymentType, String status, RentStage rentStage) {
        return paymentType == PaymentType.DEPOSIT && rentStage == RentStage.HOLD_DEPOSIT && "approved".equalsIgnoreCase(status);
    }

    private boolean isWaitingForChargeConfirmation(PaymentType paymentType, String status, RentStage rentStage) {
        return paymentType == PaymentType.CHARGE && rentStage == RentStage.CHARGE_MONEY && "approved".equalsIgnoreCase(status);
    }

    private void finishRent(Rent rent) {
        rent.markRentFinished();
        rentRepository.save(rent);
        webSocketServer.sendRentEndMessage(rent.getPowerBankId());
        reversePayment(rent);
    }

    private void holdMoneyBeforeRent(Rent rent) {
        rent.setStage(RentStage.HOLD_DEPOSIT);
        rentRepository.save(rent);
        final var userPhone = userService.getUserPhone(rent.getUserId());
        final var holdDetails = HoldDetails.builder()
                .amount(paymentService.getHoldAmount())
                .rentId(rent.getId())
                .powerBankId(rent.getPowerBankId())
                .userPhone(userPhone)
                .isPreAuth(true)
                .build();
        final var payment = paymentService.holdMoney(holdDetails);
        rent.setDepositPaymentId(payment.getId());

        rentRepository.save(rent);
        paymentService.checkForErrors(payment);
    }

    private void chargeMoneyAfterRent(Rent rent) {
        rent.setStage(RentStage.CHARGE_MONEY);

        final var userPhone = userService.getUserPhone(rent.getUserId());
        final var holdDetails = HoldDetails.builder()
                .amount(rent.getPrice())
                .rentTimeFormatted(DurationFormatUtils.formatDurationHMS(rent.getRentTime()))
                .rentId(rent.getId())
                .powerBankId(rent.getPowerBankId())
                .isPreAuth(false)
                .userPhone(userPhone)
                .build();
        final var payment = paymentService.holdMoney(holdDetails);
        rent.setChargePaymentId(payment.getId());

        rentRepository.save(rent);
        paymentService.checkForErrors(payment);
    }

    private void reversePayment(Rent rent) {
        final var payment = paymentService.reversePayment(rent.getDepositPaymentId());
//        rentRepository.save(rent);
//        paymentService.throwErrorIfUnsuccessful(payment);
    }

    private void checkAvailablePowerBanks(Rent rent) {
        final var powerBankInfo = stationService.findMaxChargedPowerBank(rent.getTakenInStationId());
        rent.setPowerBankSlot(powerBankInfo.getSlotNumber());
        rent.setPowerBankId(powerBankInfo.getPowerBankId());
        rentRepository.save(rent);
    }

    private void givePowerBank(Rent rent) {
        rent.setPowerBankUnlocked();
        rentRepository.save(rent);

        String rentedPowerBankId = stationService.unlockPowerBank(rent.getPowerBankSlot(),
                rent.getTakenInStationId());
        if (Strings.isNotEmpty(rentedPowerBankId)) {
            rent.setPowerBankId(rentedPowerBankId);
        }

        rent.setPowerBankTaken();
        rentRepository.save(rent);
        powerBankService.takeAction(rent.getPowerBankId(), rent.getId());

        webSocketServer.sendRentStartMessage(rent.getPowerBankId());
    }


    public void executeRentStep(Runnable r, Rent rent, boolean isNeedToThrow) {
        HttpException rentException = null;
        try {
            r.run();
        } catch (Exception e) {
            rentException = ExceptionUtil.convertToHttpException(e);
            log.error("Not success rent request", e);
        }
        if (rentException != null) {
            rent.setLastError(new RentError(rentException));
            rentRepository.save(rent);

            if (isNeedToThrow) {
                throw rentException;
            } else {
                webSocketServer.sendErrorMessage(rentException);
            }
        }
    }

}