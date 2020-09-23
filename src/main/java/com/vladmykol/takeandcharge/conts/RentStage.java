package com.vladmykol.takeandcharge.conts;

public enum RentStage {
    INIT,
    HOLD_DEPOSIT,
    WAIT_HOLD_DEPOSIT_CALLBACK,
    UNLOCK_POWERBANK,
    CHARGE_MONEY,
    WAIT_CHARGE_MONEY_CALLBACK,
    SUCCESSFULLY_FINISHED;
}
