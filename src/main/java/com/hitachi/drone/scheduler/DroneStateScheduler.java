package com.hitachi.drone.scheduler;

import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.enums.DroneState;
import com.hitachi.drone.repository.DroneRepository;
import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
@AllArgsConstructor
public class DroneStateScheduler {
    private final DroneRepository droneRepository;
    private static final Logger logger = LoggerFactory.getLogger(DroneStateScheduler.class);

    // 10 secs
    @Scheduled(fixedRate = 10000)
    public void updateDroneStates() {
        logger.debug("Entering Scheduler");
        // For those in delivered state, set state to returning
        // Assume every delivery, battery consumes 10%
        List<Drone> deliveringDrones = droneRepository.findByState(DroneState.DELIVERED);
        deliveringDrones.forEach(drone -> {
            logger.debug("Transition state of Drone with serial number '{}' to RETURNING", drone.getSerialNumber());
            drone.setBatteryCapacity(drone.getBatteryCapacity() - 10);
            drone.setState(DroneState.RETURNING);
        });
        droneRepository.saveAll(deliveringDrones);
    }
}
