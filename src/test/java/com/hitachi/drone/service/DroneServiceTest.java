package com.hitachi.drone.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.entity.Medication;
import com.hitachi.drone.enums.DroneModel;
import com.hitachi.drone.enums.DroneState;
import com.hitachi.drone.exceptions.BatteryLowException;
import com.hitachi.drone.exceptions.OverloadException;
import com.hitachi.drone.exceptions.ResourceNotFoundException;
import com.hitachi.drone.repository.DroneRepository;
import com.hitachi.drone.repository.MedicationRepository;
import com.hitachi.drone.service.impl.DroneService;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class DroneServiceTest {
    // Drone data
    private static final Long DRONE_ID = 1001L;
    private static final String SERIAL_NUMBER = "SERIAL_001";
    private static final DroneModel MODEL = DroneModel.CRUISERWEIGHT;
    private static final Double DRONE_WEIGHT_LIMIT = DroneModel.CRUISERWEIGHT.getMaxWeight();
    private static final Integer BATTERY_CAPACITY = 80;
    private static final Integer LOW_BATTERY = 20;
    private static final DroneState STATE = DroneState.IDLE;

    // Medication data
    private static final Long MEDICATION_ID = 2001L;
    private static final String MEDICATION_NAME = "SAMPLE_MEDICATION_NAME";
    private static final String MEDICATION_NAME_2 = "OTHER_MEDICATION_NAME";
    private static final Double MEDICATION_WEIGHT = 200.0;
    private static final String MEDICATION_CODE = "SAMPLE_CODE_01";
    private static final String MEDICATION_CODE_2 = "SAMPLE_CODE_02";

    @InjectMocks
    DroneService underTest;

    @Mock
    DroneRepository droneRepository;

    @Mock
    MedicationRepository medicationRepository;

    private Drone drone;
    private Medication medication;

    private static String imageName;
    private static String imageType;
    private static byte[] imageData;

    @BeforeAll
    static void setupImage() throws IOException {
        File imageFile = ResourceUtils.getFile("classpath:images/medicine-pic.jpg");
        imageData = Files.readAllBytes(imageFile.toPath());
        imageName = imageFile.getName();
        imageType = URLConnection.guessContentTypeFromName(imageFile.getName());
    }

    @BeforeEach
    void setUp() {
        drone = new Drone();
        drone.setSerialNumber(SERIAL_NUMBER);
        drone.setModel(MODEL);
        drone.setState(STATE);
        drone.setBatteryCapacity(BATTERY_CAPACITY);
        drone.setWeightLimit(DRONE_WEIGHT_LIMIT);

        medication = new Medication();
        medication.setName(MEDICATION_NAME);
        medication.setWeight(MEDICATION_WEIGHT);
        medication.setCode(MEDICATION_CODE);
        medication.setImageName(imageName);
        medication.setImageData(imageData);
        medication.setImageType(imageType);
    }

    @Test
    void testRegisterDroneSuccess() {
        // GIVEN
        given(droneRepository.save(drone)).willReturn(drone);

        // WHEN
        Drone result = underTest.registerDrone(drone);

        // THEN
        verify(droneRepository).save(drone);
        assertThat(result.getWeightLimit()).isEqualTo(DRONE_WEIGHT_LIMIT);
    }

    @Test
    void testLoadDroneSuccess() throws IOException {
        // GIVEN
        drone.setId(DRONE_ID);
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.of(drone));
        given(medicationRepository.findAllByDroneId(DRONE_ID)).willReturn(new ArrayList<>());

        // WHEN
        underTest.loadDrone(DRONE_ID, medication);

        // THEN
        verify(medicationRepository).save(medication);
        assertThat(drone.getState()).isEqualTo(DroneState.LOADED);
        assertThat(medication.getDrone().getSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(medication.getImageData()).isEqualTo(imageData);
        assertThat(medication.getImageName()).isEqualTo(imageName);
        assertThat(medication.getImageType()).isEqualTo(imageType);
    }

    @Test
    void testLoadDroneCannotBeFound() throws IOException {
        // GIVEN
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.empty());

        // WHEN
        assertThatThrownBy(
                () -> {
                    underTest.loadDrone(DRONE_ID, medication);
                })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Drone not found with the given input data ID: '" + DRONE_ID + "'");
    }

    @Test
    void testLoadDroneLowBattery() throws IOException {
        // GIVEN
        drone.setId(DRONE_ID);
        drone.setBatteryCapacity(LOW_BATTERY);
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.of(drone));

        // WHEN
        assertThatThrownBy(
                () -> {
                    underTest.loadDrone(DRONE_ID, medication);
                })
                .isInstanceOf(BatteryLowException.class)
                .hasMessageContaining("Drone cannot be loaded, battery below 25%");
    }

    @Test
    void testLoadDroneOverloaded() throws IOException {
        // GIVEN
        drone.setId(DRONE_ID);
        Medication existingMedication = new Medication();
        existingMedication.setId(MEDICATION_ID);
        existingMedication.setName(MEDICATION_NAME_2);
        existingMedication.setCode(MEDICATION_CODE_2);
        existingMedication.setWeight(MODEL.getMaxWeight());
        existingMedication.setImageName(imageName);
        existingMedication.setImageData(imageData);
        existingMedication.setImageType(imageType);

        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.of(drone));
        given(medicationRepository.findAllByDroneId(DRONE_ID)).willReturn(List.of(existingMedication));

        // WHEN
        assertThatThrownBy(
                () -> {
                    underTest.loadDrone(DRONE_ID, medication);
                })
                .isInstanceOf(OverloadException.class)
                .hasMessageContaining("Unable to load. Load exceeds weight limit of " + DRONE_WEIGHT_LIMIT);
    }

    @Test
    void testCheckDroneAvailabilityReturnsTrue() {
        // GIVEN
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.of(drone));

        // WHEN
        Boolean result = underTest.checkDroneAvailability(DRONE_ID);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    void testCheckDroneAvailabilityWithLowBatteryReturnsFalse() {
        // GIVEN
        drone.setBatteryCapacity(LOW_BATTERY);
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.of(drone));

        // WHEN
        Boolean result = underTest.checkDroneAvailability(DRONE_ID);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    void testCheckDroneAvailabilityWithLoadedReturnsFalse() {
        // GIVEN
        drone.setState(DroneState.LOADED);
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.of(drone));

        // WHEN
        Boolean result = underTest.checkDroneAvailability(DRONE_ID);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    void testCheckDroneAvailabilityThrowsResourceNotFound() {
        // GIVEN
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.empty());

        // WHEN
        assertThatThrownBy(
                () -> {
                    underTest.checkDroneAvailability(DRONE_ID);
                })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Drone not found with the given input data ID: '" + DRONE_ID + "'");
    }

    @Test
    void testCheckBatterySuccess() {
        // GIVEN
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.of(drone));

        // WHEN
        Integer result = underTest.checkBattery(DRONE_ID);

        // THEN
        assertThat(result).isEqualTo(BATTERY_CAPACITY);
    }

    @Test
    void testCheckBatteryThrowsResourceNotFound() {
        // GIVEN
        given(droneRepository.findById(DRONE_ID)).willReturn(Optional.empty());

        // WHEN
        assertThatThrownBy(
                () -> {
                    underTest.checkBattery(DRONE_ID);
                })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Drone not found with the given input data ID: '" + DRONE_ID + "'");
    }

}
