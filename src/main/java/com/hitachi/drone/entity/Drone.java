package com.hitachi.drone.entity;

import com.hitachi.drone.enums.DroneModel;
import com.hitachi.drone.enums.DroneState;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "drone")
public class Drone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Serial number cannot be null")
    @Column(name = "serial_number", unique = true)
    @Size(max = 100, message = "Serial number must not exceed 100 characters")
    private String serialNumber;

    @NotNull(message = "Model cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "model")
    private DroneModel model;

    @Column(name = "weight_limit")
    private Double weightLimit;

    @Min(value = 0, message = "Battery capacity cannot be less than 0")
    @Max(value = 100, message = "Battery capacity cannot be greater that 100")
    @NotNull(message = "Battery capacity cannot be null")
    @Column(name = "battery_capacity")
    private Integer batteryCapacity;

    @NotNull(message = "State cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private DroneState state;

    @OneToMany(mappedBy = "drone")
    private List<Medication> medications;
}
