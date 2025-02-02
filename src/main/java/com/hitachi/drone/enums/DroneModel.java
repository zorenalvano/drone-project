package com.hitachi.drone.enums;

import lombok.Getter;

@Getter
public enum DroneModel {
    LIGHTWEIGHT(400),
    MIDDLEWEIGHT(600),
    CRUISERWEIGHT(800),
    HEAVYWEIGHT(1000);

    private final double maxWeight;

    DroneModel(double maxWeight) {
        this.maxWeight = maxWeight;
    }

}
