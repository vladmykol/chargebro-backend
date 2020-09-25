package com.vladmykol.takeandcharge.conts;

public enum RentStage {
    INIT,
    WAIT_HOLD_DEPOSIT_CALLBACK,
    UNLOCK_POWERBANK,
    WAIT_CHARGE_MONEY_CALLBACK,
    SUCCESSFULLY_FINISHED;
}
