package com.hitachi.drone.exceptions;

public class BatteryLowException extends RuntimeException {
    public BatteryLowException() {
        super("Drone cannot be loaded, battery below 25%");
    }
}
