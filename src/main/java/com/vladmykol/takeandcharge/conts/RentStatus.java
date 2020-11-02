package com.vladmykol.takeandcharge.conts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum RentStatus {
    PREPARE {
        public List<RentStage> getStages() {
            return Arrays.asList(RentStage.CHECK, RentStage.HOLD_DEPOSIT, RentStage.UNLOCK_POWERBANK);
        }
    },
    ACTIVE {
        public List<RentStage> getStages() {
            return Arrays.asList(RentStage.POWERBANK_TAKEN, RentStage.CHARGE_MONEY);
        }
    },
    FINISHED {
        public List<RentStage> getStages() {
            return Collections.singletonList(RentStage.SUCCESSFULLY_FINISHED);
        }
    };

    public abstract List<RentStage> getStages();
}
