package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.cabinet.dto.client.ReturnPowerBankRequest;
import com.vladmykol.takeandcharge.conts.PowerBankStatus;
import com.vladmykol.takeandcharge.entity.PowerBank;
import com.vladmykol.takeandcharge.repository.PowerBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PowerBankService {
    private final PowerBankRepository powerBankRepository;

    public String returnAction(ReturnPowerBankRequest powerBankRequest) {
        final var optionalPowerBank = powerBankRepository.findById(powerBankRequest.getPowerBankId());

        if (optionalPowerBank.isPresent()) {
            if (isWasTaken(optionalPowerBank.get())) {
                optionalPowerBank.get().setStatus(PowerBankStatus.RETURNED);
                powerBankRepository.save(optionalPowerBank.get());
                return optionalPowerBank.get().getCurrentRentId();
            }
        } else {
            powerBankRepository.save(
                    PowerBank.builder()
                            .id(powerBankRequest.getPowerBankId())
                            .status(PowerBankStatus.RETURNED)
                            .build());
        }

        return null;
    }

    private boolean isWasTaken(PowerBank optionalPowerBank) {
        return optionalPowerBank.getStatus() == PowerBankStatus.TAKEN;
    }

    public void takeAction(String powerBankId, String rentId) {
        final var optionalPowerBank = powerBankRepository.findById(powerBankId);

        optionalPowerBank.ifPresent(powerBank -> {
            powerBank.setStatus(PowerBankStatus.TAKEN);
            powerBank.setCurrentRentId(rentId);
            powerBankRepository.save(powerBank);
        });
    }

    public List<PowerBank> findPowerBanksInRent(String userId) {
        return powerBankRepository.findByUserIdAndStatus(userId, PowerBankStatus.TAKEN);
    }

}
