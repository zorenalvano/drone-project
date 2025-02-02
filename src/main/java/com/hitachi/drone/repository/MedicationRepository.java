package com.hitachi.drone.repository;

import com.hitachi.drone.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findAllByDroneId(Long droneId);
}
