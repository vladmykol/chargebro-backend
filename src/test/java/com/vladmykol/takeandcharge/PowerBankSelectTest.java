package com.vladmykol.takeandcharge;

import com.vladmykol.takeandcharge.cabinet.dto.client.PowerBankInfo;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class PowerBankSelectTest {

    @Test
    void bestPowerBankToTake() {
        long baseTime = System.currentTimeMillis();

        PowerBankInfo powerBank0 = createPowerBank((short) 0, (short) 3, baseTime + 1000);
        PowerBankInfo powerBank1 = createPowerBank((short) 1, (short) 4, baseTime + 2000);
        PowerBankInfo powerBank2 = createPowerBank((short) 2, (short) 4, baseTime + 1000);

        Comparator<PowerBankInfo> compareByChargingLevelAndLastTakenDate = Comparator
                .comparing(PowerBankInfo::getPowerLevel)
                .thenComparing(PowerBankInfo::getLastTakeAt);

        var bestPowerBankToTake = List.of(powerBank0, powerBank1, powerBank2)
                .stream()
                .max(compareByChargingLevelAndLastTakenDate);

        assertThat(bestPowerBankToTake.get(), is(powerBank1));
    }

    private PowerBankInfo createPowerBank(short slot, short powerLevel, long lastTakeAtMillis) {
        PowerBankInfo powerBank = new PowerBankInfo();
        powerBank.setSlotNumber(slot);
        powerBank.setPowerLevel(powerLevel);
        powerBank.setLastTakeAt(new Date(lastTakeAtMillis));
        return powerBank;
    }
}
