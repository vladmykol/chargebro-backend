package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.config.AsyncConfiguration;
import com.vladmykol.takeandcharge.conts.RentStage;
import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.dto.RentConfirmationDto;
import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.dto.RentReportDto;
import com.vladmykol.takeandcharge.entity.Rent;
import com.vladmykol.takeandcharge.exceptions.ChargingStationException;
import com.vladmykol.takeandcharge.exceptions.RentIsNotFound;
import com.vladmykol.takeandcharge.repository.RentRepository;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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

    @Async(AsyncConfiguration.RETURN_POWER_BANK_TASK_EXECUTOR)
    public void prepareForRentFinish(String powerBankId, String stationId) {
        Optional<Rent> rent = rentRepository.findByPowerBankIdAndReturnedAtIsNullAndIsActiveRentTrue(powerBankId);

        if (rent.isEmpty()) {
            return;
        }
        SecurityUtil.setUser(rent.get().getUserId());

        rent.get().markPbReturned(stationId);
        try {
            paymentService.calcRentPrice(rent.get());

            if (rent.get().getPrice() > 0) {
                captureMoney(rent.get());
            } else {
                reverseMoney(rent.get());
            }
        } catch (Exception e) {
            rent.get().setErrorCause(e.toString());
            webSocketServer.sendPaymentErrorMessage(e.getMessage());
            throw e;
        } finally {
            rentRepository.save(rent.get());
        }
    }

    @Async(AsyncConfiguration.PAYMENT_CALLBACK_TASK_EXECUTOR)
    public void rentUpdateWithPaymentCallback(FondyResponse callbackDto) {
        final var rent = rentRepository.findById(callbackDto.getOrder_id());

        if (rent.isEmpty()) {
            throw new RentIsNotFound();
        }
        SecurityUtil.setUser(rent.get().getUserId());

        try {
            processRentUpdate(callbackDto, rent.get());
        } catch (Exception e) {
            rent.get().setErrorCause(e.toString());
            throw e;
        } finally {
            rentRepository.save(rent.get());
        }
    }

    private void processRentUpdate(FondyResponse callbackDto, Rent rent) {
        try {
            paymentService.processCallback(callbackDto);
        } catch (Exception e) {
            webSocketServer.sendPaymentErrorMessage("Payment issue");
            throw e;
        }

        if (rent.getStage() == RentStage.WAIT_HOLD_MONEY_CALLBACK) {
            try {
                tryToUnlockPowerBank(rent);
            } catch (Exception e) {
                reverseMoney(rent);
                webSocketServer.sendGeneralErrorMessage(rent.getPowerBankId());
                throw e;
            }
            webSocketServer.sendRentStartMessage(rent.getPowerBankId());
        } else if (rent.getStage() == RentStage.WAIT_CHARGE_MONEY_CALLBACK ||
                rent.getStage() == RentStage.WAIT_REVERSE_MONEY_CALLBACK) {

            rent.markRentFinished();
            webSocketServer.sendRentEndMessage(rent.getPowerBankId());
        }
    }


    private void unlockPowerBank(Rent rent) {
        String rentedPowerBankId = stationService.unlockPowerBank(rent.getPowerBankSlot(),
                rent.getTakenInStationId());
        rent.markRentStart(rentedPowerBankId);
    }

    private void holdMoneyBeforeRent(Rent rent) {
        rent.setStage(RentStage.HOLD_MONEY);
        String holdMoneyPaymentId = paymentService.holdMoney(rent.getId());
        rent.setHoldMoneyPaymentId(holdMoneyPaymentId);
        rent.setStage(RentStage.WAIT_HOLD_MONEY_CALLBACK);
    }

    private void captureMoney(Rent rent) {
        rent.setStage(RentStage.CHARGE_MONEY);
        paymentService.captureMoneyForRent(rent);
        rent.setStage(RentStage.WAIT_CHARGE_MONEY_CALLBACK);
    }


    private void reverseMoney(Rent rent) {
        rent.setStage(RentStage.REVERSE_MONEY);
        paymentService.reverseMoney(rent);
        rent.setStage(RentStage.WAIT_REVERSE_MONEY_CALLBACK);
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
