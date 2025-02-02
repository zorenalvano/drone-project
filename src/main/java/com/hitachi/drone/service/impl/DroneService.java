package com.hitachi.drone.service.impl;

import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.entity.Medication;
import com.hitachi.drone.enums.DroneState;
import com.hitachi.drone.exceptions.BatteryLowException;
import com.hitachi.drone.exceptions.OverloadException;
import com.hitachi.drone.exceptions.ResourceNotFoundException;
import com.hitachi.drone.repository.DroneRepository;
import com.hitachi.drone.repository.MedicationRepository;
import com.hitachi.drone.service.IDroneService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class DroneService implements IDroneService {
    private static final Integer LOW_BATTERY = 25;
    private final DroneRepository droneRepository;
    private final MedicationRepository medicationRepository;

    public Drone registerDrone(Drone drone) {
        drone.setWeightLimit(drone.getModel().getMaxWeight());
        return droneRepository.save(drone);
    }

    public void loadDrone(Long id, Medication medication) {

        Drone drone = droneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drone", "ID", id));

        if (drone.getBatteryCapacity() < LOW_BATTERY) {
            throw new BatteryLowException();
        }

        List<Medication> existingMedications = medicationRepository.findAllByDroneId(id);
        // Getting the total weight of already loaded medication and the new medication
        // to be loaded
        double totalWeight = existingMedications.stream().mapToDouble(Medication::getWeight).sum();

        if ((totalWeight + medication.getWeight()) > drone.getWeightLimit()) {
            throw new OverloadException(drone.getWeightLimit());
        }

        medication.setDrone(drone);

        medicationRepository.save(medication);

        // Setting the state to loaded
        if (drone.getState() == DroneState.IDLE || drone.getState() == DroneState.LOADING) {
            drone.setState(DroneState.LOADED);
            droneRepository.save(drone);
        }

    }

    @Override
    public List<Medication> getLoadedMedications(Long id) {
        Optional<Drone> optionalDrone = droneRepository.findById(id);
        if (optionalDrone.isEmpty()) {
            throw new ResourceNotFoundException("Drone", "ID", id);
        }
        return medicationRepository.findAllByDroneId(optionalDrone.get().getId());
    }

    @Override
    public Boolean checkDroneAvailability(Long id) {
        Drone drone = droneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drone", "ID", id));

        // Should be in Idle state and not in low battery
        boolean hasSufficientBattery = drone.getBatteryCapacity() >= LOW_BATTERY;
        boolean isInIdleState = drone.getState() == DroneState.IDLE;
        return hasSufficientBattery && isInIdleState;
    }

    @Override
    public Integer checkBattery(Long id) {
        Drone drone = droneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drone", "ID", id));
        return drone.getBatteryCapacity();
    }
}
