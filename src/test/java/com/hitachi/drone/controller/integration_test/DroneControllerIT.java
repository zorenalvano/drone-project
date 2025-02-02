package com.hitachi.drone.controller.integration_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.entity.Medication;
import com.hitachi.drone.enums.DroneModel;
import com.hitachi.drone.enums.DroneState;
import com.hitachi.drone.repository.DroneRepository;
import com.hitachi.drone.repository.MedicationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DroneControllerIT {
    private static final String SERIAL_NUMBER = "SERIAL_001";
    private static final DroneModel MODEL = DroneModel.CRUISERWEIGHT;
    private static final Double DRONE_WEIGHT_LIMIT = DroneModel.CRUISERWEIGHT.getMaxWeight();
    private static final Integer BATTERY_CAPACITY = 80;
    private static final DroneState STATE = DroneState.IDLE;

    // Medication data
    private static final String MEDICATION_NAME = "SAMPLE_MEDICATION_NAME";
    private static final Double MEDICATION_WEIGHT = 200.0;
    private static final String MEDICATION_CODE = "SAMPLE_CODE_01";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    DroneRepository droneRepository;

    @Autowired
    MedicationRepository medicationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static String imageName;
    private static String imageType;
    private static byte[] imageData;

    private Drone drone;
    private Medication medication;

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

    @Transactional
    @Test
    void testRegisterDrone() throws Exception {
        // WHEN
        this.mockMvc.perform(
                post("/api/drones/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value(SERIAL_NUMBER))
                .andExpect(jsonPath("$.weightLimit").value(DRONE_WEIGHT_LIMIT))
                .andExpect(jsonPath("$.model").value(MODEL.toString()))
                .andExpect(jsonPath("$.state").value(STATE.toString()))
                .andExpect(jsonPath("$.batteryCapacity").value(BATTERY_CAPACITY));
    }

    @Transactional
    @Test
    void testLoadDrone() throws Exception {
        // GIVEN
        droneRepository.save(drone);

        // WHEN
        this.mockMvc.perform(
                post("/api/drones/" + drone.getId() + "/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medication)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.statusMsg").value("Drone loaded successfully"));

        // THEN
        List<Medication> medications = medicationRepository.findAllByDroneId(drone.getId());
        assertThat(medications.size()).isEqualTo(1);
        assertThat(medications.get(0).getName()).isEqualTo(MEDICATION_NAME);
        assertThat(medications.get(0).getWeight()).isEqualTo(MEDICATION_WEIGHT);
        assertThat(medications.get(0).getCode()).isEqualTo(MEDICATION_CODE);
        assertThat(medications.get(0).getImageName()).isEqualTo(imageName);
        assertThat(medications.get(0).getImageType()).isEqualTo(imageType);
        assertThat(medications.get(0).getImageData()).isEqualTo(imageData);
    }

    @Transactional
    @Test
    void testGetMedications() throws Exception {
        // GIVEN
        droneRepository.save(drone);
        medication.setDrone(drone);
        medicationRepository.save(medication);

        // WHEN
        this.mockMvc.perform(
                get("/api/drones/" + drone.getId() + "/medications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[*].name").value(hasItem(MEDICATION_NAME)))
                .andExpect(jsonPath("$.[*].weight").value(hasItem(MEDICATION_WEIGHT)))
                .andExpect(jsonPath("$.[*].code").value(hasItem(MEDICATION_CODE)))
                .andExpect(jsonPath("$.[*].imageName").value(hasItem(imageName)))
                .andExpect(jsonPath("$.[*].imageType").value(hasItem(imageType)));
    }

    @Transactional
    @Test
    void testCheckDroneAvailability() throws Exception {
        // GIVEN
        droneRepository.save(drone);

        // WHEN
        this.mockMvc.perform(
                get("/api/drones/" + drone.getId() + "/availability"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Transactional
    @Test
    void testCheckBatterySuccess() throws Exception {
        // GIVEN
        droneRepository.save(drone);

        // WHEN
        this.mockMvc.perform(
                get("/api/drones/" + drone.getId() + "/battery"))
                .andExpect(status().isOk())
                .andExpect(content().string(BATTERY_CAPACITY.toString()));
    }
}
