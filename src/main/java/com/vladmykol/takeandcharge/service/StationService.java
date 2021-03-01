package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.cabinet.StationRegister;
import com.vladmykol.takeandcharge.cabinet.StationSocketClient;
import com.vladmykol.takeandcharge.cabinet.dto.MessageHeader;
import com.vladmykol.takeandcharge.cabinet.dto.ProtocolEntity;
import com.vladmykol.takeandcharge.cabinet.dto.RawMessage;
import com.vladmykol.takeandcharge.cabinet.dto.client.ChargingStationInventory;
import com.vladmykol.takeandcharge.cabinet.dto.client.LoginRequest;
import com.vladmykol.takeandcharge.cabinet.dto.client.PowerBankInfo;
import com.vladmykol.takeandcharge.cabinet.dto.client.TakePowerBankResponse;
import com.vladmykol.takeandcharge.cabinet.dto.server.ChangeServerAddressRequest;
import com.vladmykol.takeandcharge.cabinet.dto.server.TakePowerBankRequest;
import com.vladmykol.takeandcharge.conts.PowerBankStatus;
import com.vladmykol.takeandcharge.dto.AuthenticatedStationsDto;
import com.vladmykol.takeandcharge.dto.StationInfoDto;
import com.vladmykol.takeandcharge.entity.Station;
import com.vladmykol.takeandcharge.exceptions.NoPowerBanksLeft;
import com.vladmykol.takeandcharge.exceptions.NotSuccessesRent;
import com.vladmykol.takeandcharge.repository.PowerBankRepository;
import com.vladmykol.takeandcharge.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.vladmykol.takeandcharge.cabinet.dto.MessageHeader.MessageCommand.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {
    private final StationRegister stationRegister;
    private final PowerBankRepository powerBankRepository;
    private final StationRepository stationRepository;
    private final ModelMapper stationInfoMapper;

    public ChargingStationInventory getStationInventory(String cabinetId) {
        ProtocolEntity<?> stockRequest = new ProtocolEntity<>(CABINET_STOCK);

        ProtocolEntity<RawMessage> messageFromClient = stationRegister.communicateWithStation(cabinetId, stockRequest);

        ChargingStationInventory chargingStationInventory = messageFromClient.getBody().readTo(new ChargingStationInventory());
        for (int i = 0; i < chargingStationInventory.getRemainingPowerBanks(); i++) {
            final var powerBankInfo = messageFromClient.getBody().readTo(new PowerBankInfo());

            final var optionalPowerBank = powerBankRepository.findById(powerBankInfo.getPowerBankId());
            if (optionalPowerBank.isPresent()) {
                if (PowerBankStatus.MAINTENANCE == optionalPowerBank.get().getStatus()) {
                    continue;
                }
            }
            chargingStationInventory.getPowerBankList().add(powerBankInfo);
        }

        log.debug("Power Bank inventory request {} and {}", messageFromClient.getHeader(), chargingStationInventory);

        return chargingStationInventory;
    }

    public String unlockPowerBank(short powerBankSlot, String cabinetId) {
        ProtocolEntity<TakePowerBankRequest> powerBankRequest = new ProtocolEntity<>(TAKE_POWER_BANK,
                new TakePowerBankRequest(powerBankSlot));

        ProtocolEntity<RawMessage> messageFromClient = stationRegister.communicateWithStation(cabinetId, powerBankRequest);

        TakePowerBankResponse takePowerBankResponse = messageFromClient.getBody().readFullyTo(new TakePowerBankResponse());
        log.debug("Rent response {} and {}", messageFromClient.getHeader(), takePowerBankResponse);

        if (takePowerBankResponse.getResult() != 1) {
            if (!isPowerReallyTaken(powerBankSlot, cabinetId)) {
                throw new NotSuccessesRent();
            }
        }

        return takePowerBankResponse.getPowerBankId();
    }

    public boolean isPowerReallyTaken(short powerBankSlot, String cabinetId) {
        ChargingStationInventory chargingStationInventory = getStationInventory(cabinetId);

        if (chargingStationInventory.getPowerBankList().isEmpty()) {
            throw new NoPowerBanksLeft();
        }

        var powerBankInfoOptional = chargingStationInventory.getPowerBankList()
                .stream()
                .filter(powerBankInfo -> powerBankSlot == powerBankInfo.getSlotNumber())
                .findFirst();

        return powerBankInfoOptional.isPresent();
    }

    public List<StationInfoDto> findStationsNearBy(double x, double y) {
        Distance distance = new Distance(100, Metrics.KILOMETERS);
        Point point = new Point(x, y);
        // TODO: 9/24/2020 resolve mongo db Legacy point is out of bounds for spherical query
        try {
            var nearByStations = stationRepository.findByLocationNear(point, distance);

            return nearByStations.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return null;
        }
    }

    private StationInfoDto convertToDto(Station station) {
        return stationInfoMapper.map(station, StationInfoDto.class);
    }

    private Station convertToEntity(StationInfoDto stationInfoDto) {
        var station = stationInfoMapper.map(stationInfoDto, Station.class);
        var point = new Point(stationInfoDto.getLocationX(), stationInfoDto.getLocationY());
        station.setLocation(point);
        return station;
    }

    public void update(StationInfoDto stationInfoDto) {
        var station = convertToEntity(stationInfoDto);
        stationRepository.save(station);
    }

    public List<StationInfoDto> findAll() {
        var allStations = stationRepository.findAll();
        return allStations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public MessageHeader setServerAddressAndRestart(String cabinetId, String serverAddress, String port, short interval) {
        var changeServerAddressRequest = ChangeServerAddressRequest.builder()
                .serverAddress(serverAddress)
                .serverPort(port)
                .heartbeatIntervalSec(interval).build();

        ProtocolEntity<?> setServerAddressRequest = new ProtocolEntity<>(SET_SERVER_ADDRESS, changeServerAddressRequest);

        stationRegister.communicateWithStation(cabinetId, setServerAddressRequest);
        final var messageProtocolEntity = stationRegister.communicateWithStation(cabinetId, new ProtocolEntity<>(RESTART));

        return messageProtocolEntity.getHeader();
    }

    public void unlockAllPowerBanks(String cabinetId) {
        final var stationInventory = getStationInventory(cabinetId);

        log.debug("Eject all powerbanks for station {}", cabinetId);
        stationInventory.getPowerBankList().forEach(powerBankInfo -> {
            ProtocolEntity<TakePowerBankRequest> powerBankRequest = new ProtocolEntity<>(FORCE_POPUP,
                    new TakePowerBankRequest(powerBankInfo.getSlotNumber()));

            stationRegister.communicateWithStation(cabinetId, powerBankRequest);
        });
    }


    public int getRemainingPowerBanks(String cabinetId) {
        return getStationInventory(cabinetId).getRemainingPowerBanks();
    }


    public PowerBankInfo findMaxChargedPowerBank(String stationId) {
        ChargingStationInventory chargingStationInventory = getStationInventory(stationId);

        if (chargingStationInventory.getPowerBankList().isEmpty()) {
            throw new NoPowerBanksLeft();
        }

        var maxChargedPowerBank = chargingStationInventory.getPowerBankList()
                .stream()
                .max(Comparator.comparing(PowerBankInfo::getPowerLevel));

        if (maxChargedPowerBank.isPresent()) {
            return maxChargedPowerBank.get();
        } else {
            throw new NoPowerBanksLeft();
        }
    }


    public Station getById(String id) {
        final var optionalStation = stationRepository.findById(id);
        return optionalStation.orElseGet(Station::new);
    }

    public boolean singIn(LoginRequest loginRequest, StationSocketClient stationSocketClient) {
        final var optionalStation = stationRepository.findById(loginRequest.getBoxId());
        if (optionalStation.isPresent()) {
            optionalStation.get().setLastLogIn(new Date());
            stationRepository.save(optionalStation.get());
//first login, somehow station can send multiple login requests
            if (stationSocketClient.getClientInfo().getCabinetId() == null) {
                stationSocketClient.getClientInfo().setCabinetId(loginRequest.getBoxId());
                stationRegister.authStation(stationSocketClient);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isHardWareStationId(String id) {
        return id.contains("STW");
    }

    public String extractStationId(String url) {
        if (isHardWareStationId(url)) {
            var starIndex = url.indexOf("=");
            if (starIndex > 0) {
                return url.substring(starIndex + 1);
            } else {
                return url;
            }
        } else {
            var starIndex = url.indexOf("/k");
            String shortId;
            if (starIndex > 0) {
                shortId = url.substring(starIndex + 1);
            } else {
                shortId = url;
            }
            final var optionalStation = stationRepository.findByShortId(shortId);
            if (optionalStation.isPresent()) {
                return optionalStation.get().getId();
            } else {
                return shortId;
            }
        }
    }

    public List<AuthenticatedStationsDto> getAuthenticatedStations() {
        return stationRegister.getConnections();
    }

    public void deleteById(String stationId) {
        stationRepository.deleteById(stationId);
    }

    public MessageHeader restart(String stationId) {
        final var messageProtocolEntity = stationRegister.communicateWithStation(stationId, new ProtocolEntity<>(RESTART));

        return messageProtocolEntity.getHeader();
    }
}
