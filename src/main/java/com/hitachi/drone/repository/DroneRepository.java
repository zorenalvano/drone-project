package com.hitachi.drone.repository;

import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.enums.DroneState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroneRepository extends JpaRepository<Drone, Long> {
    List<Drone> findByState(DroneState state);
}
