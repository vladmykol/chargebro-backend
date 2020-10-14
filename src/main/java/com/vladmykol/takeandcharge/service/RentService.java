package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.conts.PaymentType;
import com.vladmykol.takeandcharge.conts.RentStage;
import com.vladmykol.takeandcharge.dto.HoldDetails;
import com.vladmykol.takeandcharge.dto.RentConfirmationDto;
import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.dto.RentReportDto;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.entity.Rent;
import com.vladmykol.takeandcharge.entity.RentError;
import com.vladmykol.takeandcharge.exceptions.ChargingStationException;
import com.vladmykol.takeandcharge.exceptions.HttpException;
import com.vladmykol.takeandcharge.exceptions.NotSuccessesRent;
import com.vladmykol.takeandcharge.exceptions.PaymentException;
import com.vladmykol.takeandcharge.repository.RentRepository;
import com.vladmykol.takeandcharge.utils.ExceptionUtil;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentService {
    private final StationService stationService;
    private final PaymentService paymentService;
    private final RentRepository rentRepository;
    private final WebSocketServer webSocketServer;
    private final UserService userService;
    private final UserWalletService userWalletService;

    public RentConfirmationDto getBeforeRentInfo(String stationId) {
//        final var powerBankInfo = stationService.findMaxChargedPowerBank(stationId);
        if (!userWalletService.isUserHasPaymentMethod(SecurityUtil.getUser())) {
            throw new PaymentException("Please add at least one valid payment card");
        }
        stationService.check(stationId);

        final var holdAmount = paymentService.getHoldAmount() / 100;
        final var userBonus = userService.getUserBonus(SecurityUtil.getUser()) / 100;
        String userBonusString;
        if (userBonus == 0) {
            userBonusString = "0";
        } else {
            userBonusString = userBonus + ".00";
        }
        return RentConfirmationDto.builder()
                .stationId(stationId)
                .holdAmount(holdAmount + ".00")
                // TODO: 9/17/2020 calc bonus
                .bonusAmount(userBonusString)
                .build();
    }

    public void syncRentStart(String cabinetId) {
        Rent rent = rentRepository.save(
                Rent.builder()
                        .stage(RentStage.INIT)
                        .takenInStationId(cabinetId)
                        .build()
        );

        executeRentStep(() -> {

            safeCheckAvailablePowerBanks(rent);
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
            processRentUpdate(payment.getType(), optionalRent.get());

        }, optionalRent.get(), false);
    }

    //    @Async(AsyncConfiguration.RETURN_POWER_BANK_TASK_EXECUTOR)
    public void updateRentWithReturnPowerBankRequest(String powerBankId, String stationId) {
        Optional<Rent> rent = rentRepository.findByPowerBankIdAndReturnedAtIsNullAndIsActiveRentTrue(powerBankId);

        if (rent.isEmpty()) {
            return;
        }
        SecurityUtil.setUser(rent.get().getUserId());

        rent.get().markPbReturned(stationId);
        //noinspection ThrowableNotThrown
        executeRentStep(() -> {

            finalizeRent(rent.get());

        }, rent.get(), false);
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

    private void processRentUpdate(PaymentType paymentType, Rent rent) {
        if (isDepositConfirmed(paymentType, rent.getStage())) {
            givePowerBank(rent);
        } else if (isChargeConfirmed(paymentType, rent.getStage())) {
            finishRent(rent);
        }
    }

    private boolean isDepositConfirmed(PaymentType paymentType, RentStage rentStage) {
        return paymentType == PaymentType.DEPOSIT && rentStage == RentStage.WAIT_HOLD_DEPOSIT_CALLBACK;
    }

    private boolean isChargeConfirmed(PaymentType paymentType, RentStage rentStage) {
        return paymentType == PaymentType.CHARGE && rentStage == RentStage.WAIT_CHARGE_MONEY_CALLBACK;
    }

    private void finishRent(Rent rent) {
        rent.markRentFinished();
        rentRepository.save(rent);
        webSocketServer.sendRentEndMessage(rent.getPowerBankId());
        reversePayment(rent);
    }

    private void holdMoneyBeforeRent(Rent rent) {
        rent.setStage(RentStage.WAIT_HOLD_DEPOSIT_CALLBACK);
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
        rent.setStage(RentStage.WAIT_CHARGE_MONEY_CALLBACK);

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
        rentRepository.save(rent);
//        paymentService.throwErrorIfUnsuccessful(payment);
    }

    private void safeCheckAvailablePowerBanks(Rent rent) {
        try {
            checkAvailablePowerBanks(rent);
        } catch (ChargingStationException e) {
            checkAvailablePowerBanks(rent);
        }
    }

    private void checkAvailablePowerBanks(Rent rent) {
        final var powerBankInfo = stationService.findMaxChargedPowerBank(rent.getTakenInStationId());
        rent.setPowerBankSlot(powerBankInfo.getSlotNumber());
        rent.setPowerBankId(powerBankInfo.getPowerBankId());
        rentRepository.save(rent);
    }

    private void safeUnlockPowerBank(Rent rent) {
        try {
            unlockPowerBank(rent);
        } catch (ChargingStationException e) {
            unlockPowerBank(rent);
        }
    }

    private void unlockPowerBank(Rent rent) {
        String rentedPowerBankId = stationService.unlockPowerBank(rent.getPowerBankSlot(),
                rent.getTakenInStationId());
        rent.markRentStart(rentedPowerBankId);
        rentRepository.save(rent);
    }


    private void givePowerBank(Rent rent) {
        rent.setStage(RentStage.UNLOCK_POWERBANK);
        rentRepository.save(rent);
        try {
            safeCheckAvailablePowerBanks(rent);
            safeUnlockPowerBank(rent);
        } catch (NotSuccessesRent e) {
//            dont know why but when station responses with this error, powerbank is unlocked sometime
            rent.setComment(e.toString());
        } catch (Exception e) {
            reversePayment(rent);
            throw e;
        }
        webSocketServer.sendRentStartMessage(rent.getPowerBankId());
    }

    public List<RentHistoryDto> getRentHistory(Boolean onlyInRent) {
        List<RentHistoryDto> rentHistoryResponse = new ArrayList<>();
        List<Rent> rentedPowerBanks;
        if (onlyInRent != null && onlyInRent) {
            rentedPowerBanks = rentRepository.findByUserIdAndIsActiveRentTrue(SecurityUtil.getUser());
// TODO: 9/25/2020     if is active, check if it is present in station
        } else {
            rentedPowerBanks = rentRepository.findByUserId(SecurityUtil.getUser());
        }

        if (!CollectionUtils.isEmpty(rentedPowerBanks)) {
            rentedPowerBanks.forEach(rentedPowerBank -> {
                rentHistoryResponse.add(
                        RentHistoryDto.builder()
                                .powerBankId(rentedPowerBank.getPowerBankId())
                                .rentPeriodMs(rentedPowerBank.getRentTime())
//                                .price(paymentService.getRentPriceAmount(rentedPowerBank.getRentTime()))
                                .isReturned((rentedPowerBank.getReturnedAt() == null) ? 0 : 1)
                                .errorCode(rentedPowerBank.getLastErrorCodeValue())
                                .errorMessage(rentedPowerBank.getLastErrorMessage())
                                .build()
                );

            });
        }
        return rentHistoryResponse;
    }

    public List<RentReportDto> getRentReport() {
//        ExampleMatcher matcher = ExampleMatcher
//                .matchingAll()
//                .withMatcher("firstName", contains().ignoreCase());
//        MarvelCharacter example = MarvelCharacter
//                .builder()
//                .firstName(firstName) // firstName from parameter
//                .lastName(lastName) // lastName from parameter
//                .build();
//        final var all = rentRepository.findAll(Example.of(example, matcher));
        final var allRent = rentRepository.findAll();


        return allRent.stream()
                .map(rent -> {
                    final var takeInStation = stationService.getById(rent.getTakenInStationId());
                    final var returnedToStation = stationService.getById(rent.getTakenInStationId());
                    return RentReportDto.builder()
                            .orderId(rent.getId())
                            .takePlace(takeInStation.getPlaceName())
                            .takeAddress(takeInStation.getAddress())
                            .returnPlace(returnedToStation.getPlaceName())
                            .returnAddress(returnedToStation.getAddress())
                            .isActiveRent(rent.isActiveRent())
                            .lastModifiedDate(rent.getLastModifiedDate())
                            .powerBankId(rent.getPowerBankId())
                            .price(rent.getPrice())
                            .depositPaymentId(rent.getDepositPaymentId())
                            .chargePaymentId(rent.getChargePaymentId())
                            .userPhone(userService.getUserPhone(rent.getUserId()))
                            .takenAt(rent.getTakenAt())
                            .returnedAt(rent.getReturnedAt())
                            .stage(rent.getStage())
                            .comment(rent.getComment())
                            .lastErrorCode(rent.getLastErrorCodeValue())
                            .lastErrorMessage(rent.getLastErrorMessage())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void executeRentStep(Runnable r, Rent rent, boolean isNeedToThrow) {
        HttpException rentException = null;
        try {
            r.run();
        } catch (Exception e) {
            rentException = ExceptionUtil.convertToHttpException(e);
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

    public void clearRent() {
        rentRepository.deleteAll();
    }

    public void refresh(String user) {
//        final var rentInProgress = rentRepository.findByUserIdAndIsActiveRentTrue(user);
//        rentInProgress.forEach(rent -> {
//           stationService.getStationInventory(rent.getReturnedToStationId())
//
//        });
    }
}