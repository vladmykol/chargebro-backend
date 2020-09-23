package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.conts.PaymentType;
import com.vladmykol.takeandcharge.conts.RentStage;
import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.dto.RentConfirmationDto;
import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.dto.RentReportDto;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.entity.Rent;
import com.vladmykol.takeandcharge.exceptions.CabinetIsOffline;
import com.vladmykol.takeandcharge.exceptions.ChargingStationException;
import com.vladmykol.takeandcharge.repository.RentRepository;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public RentConfirmationDto getBeforeRentInfo(String stationId) {
        final var powerBankInfo = stationService.findMaxChargedPowerBank(stationId);

        return RentConfirmationDto.builder()
                .holdAmount(paymentService.getHoldAmount())
                // TODO: 9/17/2020 calc bonus
                .bonusAmount(100)
                .powerLevel(powerBankInfo.getPowerLevel())
                .build();
    }

    public void prepareForRentStart(String cabinetId) {
        Rent rent = rentRepository.save(
                Rent.builder()
                        .stage(RentStage.INIT)
                        .takenInStationId(cabinetId)
                        .build()
        );

        try {
            checkAvailablePowerBanks(rent);
            holdMoneyBeforeRent(rent);
        } catch (Exception e) {
            rent.setErrorCause(e.toString());
            throw e;
        } finally {
            rentRepository.save(rent);
        }
    }


    public void rentUpdateWithPaymentCallback(FondyResponse callbackDto) {
        Optional<Rent> rent = Optional.empty();
        try {
            final var payment = paymentService.processCallback(callbackDto);

            rent = rentRepository.findById(payment.getRentId());
            if (rent.isEmpty()) {
                log.error("Rent with id {} is not found", payment.getRentId());
                return;
            }

            processRentUpdate(payment, rent.get());
        } catch (Exception e) {
            rent.ifPresent(value -> value.setErrorCause(e.toString()));
        } finally {
            rent.ifPresent(rentRepository::save);
        }
    }

    //    @Async(AsyncConfiguration.RETURN_POWER_BANK_TASK_EXECUTOR)
    public void prepareForRentFinish(String powerBankId, String stationId) {
        Optional<Rent> rent = rentRepository.findByPowerBankIdAndReturnedAtIsNullAndIsActiveRentTrue(powerBankId);

        if (rent.isEmpty()) {
            return;
        }
        SecurityUtil.setUser(rent.get().getUserId());

        rent.get().markPbReturned(stationId);
        rentRepository.save(rent.get());
        try {

            final var rentPriceAmount = paymentService.getRentPriceAmount(rent.get().getRentTime());
            rent.get().setPrice(rentPriceAmount);

            reversePayment(rent.get().getDepositPaymentId());
            if (rentPriceAmount > 0) {
                chargeMoneyAfterRent(rent.get());
            } else {
                finishRent(rent.get());
            }

        } catch (Exception e) {
            rent.get().setErrorCause(e.toString());
            webSocketServer.sendPaymentErrorMessage(e.getMessage());
        } finally {
            rentRepository.save(rent.get());
        }
    }

    private void processRentUpdate(Payment payment, Rent rent) {
        if (payment.getType() == PaymentType.DEPOSIT && rent.getStage() == RentStage.WAIT_HOLD_DEPOSIT_CALLBACK) {
            try {
                tryToUnlockPowerBank(rent);
            } catch (Exception e) {
                reversePayment(rent.getDepositPaymentId());
                webSocketServer.sendGeneralErrorMessage("Was not able to unlock a powerbank. Please try again later");
                throw e;
            }
            webSocketServer.sendRentStartMessage(rent.getPowerBankId());
        } else if ((rent.getStage() == RentStage.WAIT_CHARGE_MONEY_CALLBACK && payment.getType() == PaymentType.CHARGE)) {
            finishRent(rent);
        }
    }

    private void finishRent(Rent rent) {
        rent.markRentFinished();
        webSocketServer.sendRentEndMessage(rent.getPowerBankId());
    }


    private void unlockPowerBank(Rent rent) {
        String rentedPowerBankId = stationService.unlockPowerBank(rent.getPowerBankSlot(),
                rent.getTakenInStationId());
        rent.markRentStart(rentedPowerBankId);
    }

    private void holdMoneyBeforeRent(Rent rent) {
        rent.setStage(RentStage.HOLD_DEPOSIT);
        rentRepository.save(rent);
        String holdMoneyPaymentId = paymentService.holdMoney(rent.getId(),
                true, paymentService.getHoldAmount());
        rent.setDepositPaymentId(holdMoneyPaymentId);
        rent.setStage(RentStage.WAIT_HOLD_DEPOSIT_CALLBACK);
        rentRepository.save(rent);
    }

    private void chargeMoneyAfterRent(Rent rent) {
        rent.setStage(RentStage.CHARGE_MONEY);
        rentRepository.save(rent);
        String paymentId = paymentService.holdMoney(rent.getId(),
                false, rent.getPrice());
        rent.setChargePaymentId(paymentId);
        rent.setStage(RentStage.WAIT_CHARGE_MONEY_CALLBACK);
        rentRepository.save(rent);
    }

    private void reversePayment(String paymentId) {
        paymentService.reversePayment(paymentId);
    }

    private void checkAvailablePowerBanks(Rent rent) {
        final var powerBankSlot = stationService.findMaxChargedPowerBank(rent.getTakenInStationId()).getSlotNumber();
        rent.setPowerBankSlot(powerBankSlot);
    }


    private void tryToUnlockPowerBank(Rent rent) {
        rent.setStage(RentStage.UNLOCK_POWERBANK);
        try {
            checkAvailablePowerBanks(rent);
            unlockPowerBank(rent);
        } catch (CabinetIsOffline e) {
            checkAvailablePowerBanks(rent);
            unlockPowerBank(rent);
        } catch (ChargingStationException e) {
            checkAvailablePowerBanks(rent);
            unlockPowerBank(rent);
        }
    }

    public List<RentHistoryDto> getRentHistory(Boolean onlyInRent) {
        List<RentHistoryDto> rentHistoryResponse = new ArrayList<>();
        List<Rent> rentedPowerBanks;
        if (onlyInRent != null && onlyInRent) {
            rentedPowerBanks = rentRepository.findByUserIdAndIsActiveRentTrue(SecurityUtil.getUser());
        } else {
            rentedPowerBanks = rentRepository.findByUserId(SecurityUtil.getUser());
        }
        if (!CollectionUtils.isEmpty(rentedPowerBanks)) {
            rentedPowerBanks.forEach(rentedPowerBank -> {
                rentHistoryResponse.add(
                        RentHistoryDto.builder()
                                .powerBankId(rentedPowerBank.getPowerBankId())
                                .rentPeriodMs(rentedPowerBank.getRentTime())
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
                            .errorCause(rent.getErrorCause())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
