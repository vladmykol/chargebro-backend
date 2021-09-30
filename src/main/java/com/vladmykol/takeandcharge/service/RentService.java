package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.conts.RentStage;
import com.vladmykol.takeandcharge.conts.RentStatus;
import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.dto.RentReportDto;
import com.vladmykol.takeandcharge.entity.Rent;
import com.vladmykol.takeandcharge.repository.RentRepository;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentService {
    private final RentRepository rentRepository;
    private final StationServiceHelper stationServiceHelper;
    private final UserService userService;

    public List<Rent> getAllActiveRentWithNotReturnedPowerBank() {
        return rentRepository.findByStageIn(RentStatus.ACTIVE.getStages());
    }

    public List<Rent> getActiveRentWithNotReturnedPowerBank(String userId) {
        return rentRepository.findByUserIdAndStageIn(userId
                , RentStatus.ACTIVE.getStages());
    }

    public List<Rent> getActiveRentWithNotReturnedPowerBank() {
        return getActiveRentWithNotReturnedPowerBank(SecurityUtil.getUser());
    }

    public boolean isUserHasActiveRent() {
        return !getActiveRentWithNotReturnedPowerBank().isEmpty();
    }

    public List<RentHistoryDto> getRentHistory(Boolean onlyInRent) {
        List<RentHistoryDto> rentHistoryResponse = new ArrayList<>();
        List<Rent> rentedPowerBanks;
        if (onlyInRent != null && onlyInRent) {
            rentedPowerBanks = getActiveRentWithNotReturnedPowerBank();
// TODO: 9/25/2020     if is active, check if it is present in station
        } else {
            rentedPowerBanks = rentRepository.findByUserIdAndPowerBankReturnedAtNotNull(SecurityUtil.getUser());
        }

        rentedPowerBanks.forEach(rentedPowerBank -> {
            rentHistoryResponse.add(
                    RentHistoryDto.builder()
                            .powerBankId(rentedPowerBank.getPowerBankId())
                            .rentPeriodMs(rentedPowerBank.getRentTime())
                            .rentPrice(rentedPowerBank.getPrice())
                            .rentStartTime(rentedPowerBank.getPowerBankTakenAt().getTime())
                            .isReturned((rentedPowerBank.getPowerBankReturnedAt() == null) ? 0 : 1)
                            .errorCode(rentedPowerBank.getLastErrorCodeValue())
                            .errorMessage(rentedPowerBank.getLastErrorMessage())
                            .build()
            );

        });
        return rentHistoryResponse;
    }

    public List<RentReportDto> getRentReport() {
        final var allRent = rentRepository.findAll();

        return allRent.stream()
                .map(rent -> {
                    final var takeInStation = stationServiceHelper.getByIdOrNew(rent.getTakenInStationId());
                    final var returnedToStation = stationServiceHelper.getByIdOrNew(rent.getTakenInStationId());
                    return RentReportDto.builder()
                            .orderId(rent.getId())
                            .takeStation(takeInStation.getShortId())
                            .takePlace(takeInStation.getPlaceName())
                            .takeAddress(takeInStation.getAddress())
                            .returnStation(returnedToStation.getShortId())
                            .returnPlace(returnedToStation.getPlaceName())
                            .returnAddress(returnedToStation.getAddress())
                            .lastModifiedDate(rent.getLastModifiedDate())
                            .powerBankId(rent.getPowerBankId())
                            .price(rent.getPrice())
                            .depositPaymentId(rent.getDepositPaymentId())
                            .chargePaymentId(rent.getChargePaymentId())
                            .userPhone(userService.getUserPhone(rent.getUserId()))
                            .takenAt(rent.getPowerBankTakenAt())
                            .returnedAt(rent.getPowerBankReturnedAt())
                            .stage(rent.getStage())
                            .comment(rent.getComment())
                            .lastErrorCode(rent.getLastErrorCodeValue())
                            .lastErrorMessage(rent.getLastErrorMessage())
                            .build();
                })
                .collect(Collectors.toList());
    }


    public void clearRentRow(String rentId) {
        Optional<Rent> rent = rentRepository.findById(rentId);
        if (rent.isPresent()) {
            rent.get().setStage(RentStage.TERMINATED);
            rentRepository.save(rent.get());
        }
    }
}