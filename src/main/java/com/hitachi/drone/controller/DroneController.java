package com.hitachi.drone.controller;

import com.hitachi.drone.dto.ResponseDto;
import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.entity.Medication;
import com.hitachi.drone.service.IDroneService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/drones")
public class DroneController {
    private final IDroneService droneService;

    @PostMapping("/register")
    public ResponseEntity<Drone> registerDrone(@Valid @RequestBody Drone drone) {
        return ResponseEntity.status(HttpStatus.CREATED).body(droneService.registerDrone(drone));
    }

    @PostMapping("/{id}/load")
    public ResponseEntity<ResponseDto> loadDrone(@PathVariable Long id,
            @RequestBody @Valid Medication medication) {
        droneService.loadDrone(id, medication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDto(HttpStatus.CREATED.value(), "Drone loaded successfully"));
    }

    @GetMapping("/{id}/medications")
    public ResponseEntity<List<Medication>> getMedications(@PathVariable Long id) {
        return ResponseEntity.ok(droneService.getLoadedMedications(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkDroneAvailability(@PathVariable Long id) {
        boolean isAvailable = droneService.checkDroneAvailability(id);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/{id}/battery")
    public ResponseEntity<Integer> checkBattery(@PathVariable Long id) {
        return ResponseEntity.ok(droneService.checkBattery(id));
    }

}
