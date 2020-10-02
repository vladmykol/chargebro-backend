package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.config.AsyncConfiguration;
import com.vladmykol.takeandcharge.conts.PaymentType;
import com.vladmykol.takeandcharge.conts.RentStage;
import com.vladmykol.takeandcharge.dto.RentConfirmationDto;
import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.dto.RentReportDto;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.entity.Rent;
import com.vladmykol.takeandcharge.entity.RentError;
import com.vladmykol.takeandcharge.exceptions.*;
import com.vladmykol.takeandcharge.repository.RentRepository;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

    public RentConfirmationDto getBeforeRentInfo(String stationId) {
        final var serviceById = stationService.getById(stationId);

        if (serviceById.getLastLogIn() != null) {
            long diffInMillies = Math.abs(new Date().getTime() - serviceById.getLastLogIn().getTime());
            final var lastSeenMinBefore = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
            if (lastSeenMinBefore > 3) {
                throw new CabinetIsOffline();
            }
        } else {
            throw new CabinetIsOffline();
        }

        final var powerBankInfo = stationService.findMaxChargedPowerBank(stationId);

        return RentConfirmationDto.builder()
                .holdAmount(paymentService.getHoldAmount())
                // TODO: 9/17/2020 calc bonus
                .bonusAmount(100)
                .powerLevel(powerBankInfo.getPowerLevel())
                .build();
    }

    public void syncRentStart(String cabinetId) {
        Rent rent = rentRepository.save(
                Rent.builder()
                        .stage(RentStage.INIT)
                        .takenInStationId(cabinetId)
                        .build()
        );

        executeRentStepWithException(() -> {

            safeCheckAvailablePowerBanks(rent);
            holdMoneyBeforeRent(rent);

        }, rent);
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

        }, optionalRent.get());
    }

    @Async(AsyncConfiguration.RETURN_POWER_BANK_TASK_EXECUTOR)
    public void updateRentWithReturnPowerBankRequest(String powerBankId, String stationId) {
        Optional<Rent> rent = rentRepository.findByPowerBankIdAndReturnedAtIsNullAndIsActiveRentTrue(powerBankId);

        if (rent.isEmpty()) {
            return;
        }
        SecurityUtil.setUser(rent.get().getUserId());

        rent.get().markPbReturned(stationId);
        executeRentStep(() -> {

            finalizeRent(rent.get());

        }, rent.get());
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
        reversePayment(rent);
        rent.markRentFinished();
        webSocketServer.sendRentEndMessage(rent.getPowerBankId());
        rentRepository.save(rent);
    }

    private void holdMoneyBeforeRent(Rent rent) {
        rent.setStage(RentStage.WAIT_HOLD_DEPOSIT_CALLBACK);
        final var payment = paymentService.holdMoney(rent.getId(),
                true, paymentService.getHoldAmount());
        rent.setDepositPaymentId(payment.getId());

        rentRepository.save(rent);
        paymentService.checkForErrors(payment);
    }

    private void chargeMoneyAfterRent(Rent rent) {
        rent.setStage(RentStage.WAIT_CHARGE_MONEY_CALLBACK);
        final var payment = paymentService.holdMoney(rent.getId(),
                false, rent.getPrice());
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
        rentRepository.save(rent);
    }

    private void checkAvailablePowerBanks(Rent rent) {
        final var powerBankSlot = stationService.findMaxChargedPowerBank(rent.getTakenInStationId()).getSlotNumber();
        rent.setPowerBankSlot(powerBankSlot);
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
    }


    private void givePowerBank(Rent rent) {
        rent.setStage(RentStage.UNLOCK_POWERBANK);
        try {
            safeCheckAvailablePowerBanks(rent);
            safeUnlockPowerBank(rent);
        } catch (Exception e) {
            reversePayment(rent);
            rentRepository.save(rent);
            throw e;
        }
        webSocketServer.sendRentStartMessage(rent.getPowerBankId());
        rentRepository.save(rent);
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
                                .price(paymentService.getRentPriceAmount(rentedPowerBank.getRentTime()))
                                .isActive(rentedPowerBank.isActiveRent())
                                .errorCode(rentedPowerBank.getLastErrorCode().value())
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
                            .userPhone(userService.getUserPhone(rent.getUserId()))
                            .takenAt(rent.getTakenAt())
                            .returnedAt(rent.getReturnedAt())
                            .stage(rent.getStage())
                            .lastErrorCode(rent.getLastErrorCode().value())
                            .lastErrorMessage(rent.getLastErrorMessage())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public RentError executeRentStep(Runnable r, Rent rent) {
        RentError rentError = null;
        try {
            r.run();
        } catch (PaymentException e) {
            rentError = new RentError(HttpStatus.PAYMENT_REQUIRED, e.getMessage(), e);
        } catch (CabinetIsOffline e) {
            rentError = new RentError(HttpStatus.PRECONDITION_FAILED, "Cabinet is offline", e);
        } catch (NoPowerBanksLeft e) {
            rentError = new RentError(HttpStatus.PRECONDITION_FAILED, "No powerbanks left", e);
        } catch (StationCommunicatingException e) {
            rentError = new RentError(HttpStatus.INTERNAL_SERVER_ERROR, "Station protocol exception", e);
        } catch (Exception e) {
            rentError = new RentError(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        if (rentError != null) {
            rent.setLastError(rentError);
            rentRepository.save(rent);
            webSocketServer.sendErrorMessage(rentError.getCode().value(), rentError.getMessage());
        }

        return rentError;
    }

    public void executeRentStepWithException(Runnable r, Rent rent) {
        final var rentError = executeRentStep(r, rent);
        if (rentError != null) {
            throw new RentException(rentError.getCode(), rentError.getMessage());
        }
    }
}