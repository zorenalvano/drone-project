package com.hitachi.drone.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "medication")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Invalid name format")
    @NotNull(message = "Name cannot be null")
    @Column(name = "name")
    private String name;

    @NotNull(message = "Weight cannot be null")
    @Column(name = "weight")
    private Double weight;

    @NotNull(message = "Code cannot be null")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Invalid code format")
    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "image_name")
    private String imageName;

    @Lob
    @Column(name = "image_data", columnDefinition = "BLOB")
    private byte[] imageData;

    @Column(name = "image_type")
    private String imageType;

    @ManyToOne
    @JoinColumn(name = "drone_id")
    @JsonIgnoreProperties("medications")
    private Drone drone;

}
