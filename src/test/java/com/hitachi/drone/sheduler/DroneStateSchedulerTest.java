package com.hitachi.drone.sheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.enums.DroneModel;
import com.hitachi.drone.enums.DroneState;
import com.hitachi.drone.repository.DroneRepository;
import com.hitachi.drone.scheduler.DroneStateScheduler;

@ExtendWith(MockitoExtension.class)
public class DroneStateSchedulerTest {
    private static final Long DRONE_ID = 1001L;
    private static final String SERIAL_NUMBER = "SERIAL_001";
    private static final DroneModel MODEL = DroneModel.CRUISERWEIGHT;
    private static final Double DRONE_WEIGHT_LIMIT = DroneModel.CRUISERWEIGHT.getMaxWeight();
    private static final Integer BATTERY_CAPACITY = 80;
    private static final DroneState STATE = DroneState.DELIVERED;

    @InjectMocks
    DroneStateScheduler underTest;

    @Mock
    DroneRepository droneRepository;

    private Drone drone;

    @BeforeEach
    void setUp() {
        drone = new Drone();
        drone.setSerialNumber(SERIAL_NUMBER);
        drone.setModel(MODEL);
        drone.setState(STATE);
        drone.setBatteryCapacity(BATTERY_CAPACITY);
        drone.setWeightLimit(DRONE_WEIGHT_LIMIT);
        drone.setId(DRONE_ID);
    }

    @Test
    void testUpdateDroneStates() {
        given(droneRepository.findByState(DroneState.DELIVERED)).willReturn(List.of(drone));

        // WHEN
        underTest.updateDroneStates();

        // THEN
        verify(droneRepository).saveAll(anyList());
        assertThat(drone.getBatteryCapacity()).isEqualTo(BATTERY_CAPACITY - 10);
        assertThat(drone.getState()).isEqualTo(DroneState.RETURNING);
    }
}
