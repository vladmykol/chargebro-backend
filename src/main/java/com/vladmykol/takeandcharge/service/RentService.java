package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.cabinet.StationListener;
import com.vladmykol.takeandcharge.cabinet.StationSocketClient;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.dto.client.ChargingStationInventory;
import com.vladmykol.takeandcharge.cabinet.dto.client.PowerBankInfo;
import com.vladmykol.takeandcharge.cabinet.dto.client.TakePowerBankResponse;
import com.vladmykol.takeandcharge.cabinet.dto.server.TakePowerBankRequest;
import com.vladmykol.takeandcharge.config.AsyncConfiguration;
import com.vladmykol.takeandcharge.dto.RentHistoryDto;
import com.vladmykol.takeandcharge.entity.RentHistory;
import com.vladmykol.takeandcharge.exceptions.NoPowerBanksLeft;
import com.vladmykol.takeandcharge.exceptions.NotSuccessesRent;
import com.vladmykol.takeandcharge.repository.RentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.CABINET_STOCK;
import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.TAKE_POWER_BANK;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentService {
    private final StationListener stationListener;
    private final RentHistoryRepository rentHistoryRepository;
    private final RentWebSocket rentWebSocket;
    private final ModelMapper stationInfoMapper;

    public ChargingStationInventory getStationInventory(String cabinetId) {
        ProtocolEntity<?> stockRequest = new ProtocolEntity<>(CABINET_STOCK);

        StationSocketClient stationSocketClient = stationListener.getClient(cabinetId);
        ProtocolEntity<RawMessage> messageFromClient = stationSocketClient.communicate(stockRequest);

        ChargingStationInventory chargingStationInventory = messageFromClient.getBody().readTo(new ChargingStationInventory());
        for (int i = 0; i < chargingStationInventory.getRemainingPowerBanks(); i++) {
            chargingStationInventory.getPowerBankList().add(messageFromClient.getBody().readTo(new PowerBankInfo()));
        }

        log.info("Power Bank inventory request {} and {}", messageFromClient.getHeader(), chargingStationInventory);

        return chargingStationInventory;
    }

    public String rent(String cabinetId) {
        ChargingStationInventory chargingStationInventory = getStationInventory(cabinetId);

        if (chargingStationInventory.getPowerBankList().isEmpty()) {
            throw new NoPowerBanksLeft();
        }

        Optional<PowerBankInfo> maxChargedPowerBank = chargingStationInventory.getPowerBankList()
                .stream()
                .max(Comparator.comparing(PowerBankInfo::getPowerLevel));

        ProtocolEntity<TakePowerBankRequest> powerBankRequest = new ProtocolEntity<>(TAKE_POWER_BANK,
                new TakePowerBankRequest(maxChargedPowerBank.get().getSlotNumber()));

        StationSocketClient stationSocketClient = stationListener.getClient(cabinetId);
        ProtocolEntity<RawMessage> messageFromClient = stationSocketClient.communicate(powerBankRequest);

        TakePowerBankResponse takePowerBankResponse = messageFromClient.getBody().readFullyTo(new TakePowerBankResponse());
        log.info("Rent response {} and {}", messageFromClient.getHeader(), takePowerBankResponse);

        if (takePowerBankResponse.getResult() != 1) {
            throw new NotSuccessesRent();
        } else {
            RentHistory rentHistory = RentHistory.builder()
                    .powerBankId(takePowerBankResponse.getPowerBankId())
                    .build();
            rentHistoryRepository.save(rentHistory);
        }

        return takePowerBankResponse.getPowerBankId();
    }

    @Async(AsyncConfiguration.returnRentTaskExecutorName)
    public void returnRent(String powerBankId) {
        Optional<RentHistory> rentedPowerBank = rentHistoryRepository.findByPowerBankIdAndReturnedAtIsNull(powerBankId);
        rentedPowerBank.ifPresent(aRentedPowerBank -> {
            rentWebSocket.sendPowerBankReturnedMessage(aRentedPowerBank.getUserId(), aRentedPowerBank.getPowerBankId());

            aRentedPowerBank.setReturnedAt(new Date());
            rentHistoryRepository.save(aRentedPowerBank);
        });
    }

    public List<RentHistoryDto> getRentHistory(String clientId, boolean onlyInRent) {
        List<RentHistoryDto> rentHistoryResponse = new ArrayList<>();
        List<RentHistory> rentedPowerBanks;
        if (onlyInRent) {
            rentedPowerBanks = rentHistoryRepository.findByUserIdAndReturnedAtIsNull(clientId);
        } else {
            rentedPowerBanks = rentHistoryRepository.findByUserId(clientId);
        }
        if (!CollectionUtils.isEmpty(rentedPowerBanks)) {
            rentedPowerBanks.forEach(rentedPowerBank -> {
                long returnedAt = rentedPowerBank.getReturnedAt() == null ? System.currentTimeMillis() : rentedPowerBank.getReturnedAt().getTime();
                long rentPeriodMs = Math.abs(returnedAt - rentedPowerBank.getRentAt().getTime());
                rentHistoryResponse.add(
                        RentHistoryDto.builder()
                                .powerBankId(rentedPowerBank.getPowerBankId())
                                .rentPeriodMs(rentPeriodMs)
                                .build()
                );

            });
        }
        return rentHistoryResponse;
    }

    public int getRemainingPowerBanks(String cabinetId) {
        return getStationInventory(cabinetId).getRemainingPowerBanks();
    }
}
