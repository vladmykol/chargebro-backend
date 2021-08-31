package com.vladmykol.takeandcharge;

import com.vladmykol.takeandcharge.cabinet.dto.client.PowerBankInfo;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class PowerBankSelect {

    @Test
    void bestPowerBankToTake() {
        var powerBank0 = new PowerBankInfo();
        var date0 = new Date();
        powerBank0.setSlotNumber((short)0);
        powerBank0.setLastTakeAt(plusSec(date0, 1));
        powerBank0.setPowerLevel((short) 3);

        var powerBank1 = new PowerBankInfo();
        var date1 = new Date();
        powerBank1.setSlotNumber((short)1);
        powerBank1.setLastTakeAt(plusSec(date1, 2));
        powerBank1.setPowerLevel((short) 4);

        var powerBank2 = new PowerBankInfo();
        var date2 = new Date();
        powerBank2.setSlotNumber((short)2);
        powerBank2.setLastTakeAt(plusSec(date2, 1));
        powerBank2.setPowerLevel((short) 4);

        var powerBank3 = new PowerBankInfo();
        var date3 = new Date();
        powerBank3.setSlotNumber((short)3);
        powerBank3.setLastTakeAt(plusSec(date3, 4));
        powerBank3.setPowerLevel((short) 4);


        Comparator<PowerBankInfo> compareByChargingLevelAndLastTakenDate = Comparator
                .comparing(PowerBankInfo::getPowerLevel)
                .thenComparing(PowerBankInfo::getLastTakeAt);

        var bestPowerBankToTake = List.of(powerBank0, powerBank1, powerBank2)
                .stream()
                .max(compareByChargingLevelAndLastTakenDate);

        assertThat(bestPowerBankToTake.get(), is(powerBank1));
    }

    private Date plusSec(Date date, int sec) {
        date.setTime(date.getTime() + sec * 1000L);
        return date;
    }


}
