package com.hitachi.drone.service;

import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.entity.Medication;

import java.util.List;

public interface IDroneService {
    Drone registerDrone(Drone drone);

    void loadDrone(Long id, Medication medication);

    List<Medication> getLoadedMedications(Long droneId);

    Boolean checkDroneAvailability(Long droneId);

    Integer checkBattery(Long droneId);
}
