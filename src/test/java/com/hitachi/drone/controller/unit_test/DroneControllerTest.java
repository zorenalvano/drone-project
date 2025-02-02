package com.hitachi.drone.controller.unit_test;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitachi.drone.controller.DroneController;
import com.hitachi.drone.entity.Drone;
import com.hitachi.drone.entity.Medication;
import com.hitachi.drone.enums.DroneModel;
import com.hitachi.drone.enums.DroneState;
import com.hitachi.drone.exceptions.BatteryLowException;
import com.hitachi.drone.exceptions.OverloadException;
import com.hitachi.drone.exceptions.ResourceNotFoundException;
import com.hitachi.drone.service.IDroneService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.hasItem;

@WebMvcTest(DroneController.class)
public class DroneControllerTest {
        // Drone data
        private static final Long DRONE_ID = 1001L;
        private static final String SERIAL_NUMBER = "SERIAL_001";
        private static final DroneModel MODEL = DroneModel.CRUISERWEIGHT;
        private static final Double DRONE_WEIGHT_LIMIT = DroneModel.CRUISERWEIGHT.getMaxWeight();
        private static final Integer BATTERY_CAPACITY = 80;
        private static final DroneState STATE = DroneState.IDLE;
        // Medication data
        private static final Long MEDICATION_ID = 2001L;
        private static final String MEDICATION_NAME = "SAMPLE_MEDICATION_NAME";
        private static final Double MEDICATION_WEIGHT = 200.0;
        private static final String MEDICATION_CODE = "SAMPLE_CODE_01";

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private IDroneService droneService;

        @Autowired
        private ObjectMapper objectMapper;

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
                drone.setId(DRONE_ID);
                medication = new Medication();
                medication.setName(MEDICATION_NAME);
                medication.setWeight(MEDICATION_WEIGHT);
                medication.setCode(MEDICATION_CODE);
                medication.setImageName(imageName);
                medication.setImageData(imageData);
                medication.setImageType(imageType);
                medication.setId(MEDICATION_ID);
        }

        @Test
        void testRegisterDroneSuccess() throws Exception {
                // GIVEN
                given(droneService.registerDrone(any(Drone.class))).willReturn(drone);
                // WHEN
                this.mockMvc.perform(
                                post("/api/drones/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(drone)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.serialNumber").value(SERIAL_NUMBER))
                                .andExpect(jsonPath("$.weightLimit").value(DRONE_WEIGHT_LIMIT));
        }

        @Test
        void testLoadDroneSuccess() throws Exception {
                // WHEN
                this.mockMvc.perform(
                                post("/api/drones/" + DRONE_ID + "/load")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(medication)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.statusCode").value(HttpStatus.CREATED.value()))
                                .andExpect(jsonPath("$.statusMsg").value("Drone loaded successfully"));
                // THEN
                verify(droneService).loadDrone(argThat(item -> item.equals(DRONE_ID)), any(Medication.class));
        }

        @Test
        void testLoadDroneThrowsResourceNotFoundException() throws Exception {
                // GIVEN
                doThrow(new ResourceNotFoundException("Drone", "ID", DRONE_ID))
                                .when(droneService)
                                .loadDrone(argThat(item -> item.equals(DRONE_ID)), any(Medication.class));
                // WHEN
                this.mockMvc.perform(
                                post("/api/drones/" + DRONE_ID + "/load")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(medication)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()))
                                .andExpect(jsonPath("$.errorMessage")
                                                .value("Drone not found with the given input data ID: '" + DRONE_ID
                                                                + "'"));
        }

        @Test
        void testLoadDroneThrowsBatteryLowException() throws Exception {
                // GIVEN
                doThrow(new BatteryLowException())
                                .when(droneService)
                                .loadDrone(argThat(item -> item.equals(DRONE_ID)), any(Medication.class));
                // WHEN
                this.mockMvc.perform(
                                post("/api/drones/" + DRONE_ID + "/load")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(medication)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()))
                                .andExpect(jsonPath("$.errorMessage")
                                                .value("Drone cannot be loaded, battery below 25%"));
        }

        @Test
        void testLoadDroneThrowsOverLoadException() throws Exception {
                // GIVEN
                doThrow(new OverloadException(DRONE_WEIGHT_LIMIT))
                                .when(droneService)
                                .loadDrone(argThat(item -> item.equals(DRONE_ID)), any(Medication.class));
                // WHEN
                this.mockMvc.perform(
                                post("/api/drones/" + DRONE_ID + "/load")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(medication)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()))
                                .andExpect(jsonPath("$.errorMessage")
                                                .value("Unable to load. Load exceeds weight limit of "
                                                                + DRONE_WEIGHT_LIMIT));
        }

        @Test
        void testGetMedicationsSuccess() throws Exception {
                // GIVEN
                given(droneService.getLoadedMedications(DRONE_ID)).willReturn(List.of(medication));
                // WHEN
                this.mockMvc.perform(
                                get("/api/drones/" + DRONE_ID + "/medications"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.size()").value(1))
                                .andExpect(jsonPath("$.[*].name").value(hasItem(MEDICATION_NAME)))
                                .andExpect(jsonPath("$.[*].weight").value(hasItem(MEDICATION_WEIGHT)))
                                .andExpect(jsonPath("$.[*].code").value(hasItem(MEDICATION_CODE)))
                                .andExpect(jsonPath("$.[*].imageName").value(hasItem(imageName)))
                                .andExpect(jsonPath("$.[*].imageType").value(hasItem(imageType)));
        }

        @Test
        void testGetMedicationsThrowsResourceNotFoundException() throws Exception {
                // GIVEN
                given(droneService.getLoadedMedications(DRONE_ID))
                                .willThrow(new ResourceNotFoundException("Drone", "ID", DRONE_ID));
                // WHEN
                this.mockMvc.perform(
                                get("/api/drones/" + DRONE_ID + "/medications"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()))
                                .andExpect(jsonPath("$.errorMessage")
                                                .value("Drone not found with the given input data ID: '" + DRONE_ID
                                                                + "'"));
        }

        @Test
        void testCheckDroneAvailabilitySuccess() throws Exception {
                // GIVEN
                given(droneService.checkDroneAvailability(DRONE_ID)).willReturn(true);
                // WHEN
                this.mockMvc.perform(
                                get("/api/drones/" + DRONE_ID + "/availability"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("true"));
        }

        @Test
        void testCheckDroneAvailabilityThrowsResourceNotFoundException() throws Exception {
                // GIVEN
                given(droneService.checkDroneAvailability(DRONE_ID))
                                .willThrow(new ResourceNotFoundException("Drone", "ID", DRONE_ID));
                // WHEN
                this.mockMvc.perform(
                                get("/api/drones/" + DRONE_ID + "/availability"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()))
                                .andExpect(jsonPath("$.errorMessage")
                                                .value("Drone not found with the given input data ID: '" + DRONE_ID
                                                                + "'"));
        }

        @Test
        void testCheckBatterySuccess() throws Exception {
                // GIVEN
                given(droneService.checkBattery(DRONE_ID)).willReturn(BATTERY_CAPACITY);
                // WHEN
                this.mockMvc.perform(
                                get("/api/drones/" + DRONE_ID + "/battery"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(BATTERY_CAPACITY.toString()));
        }

        @Test
        void testCheckDroneBatteryThrowsResourceNotFoundException() throws Exception {
                // GIVEN
                given(droneService.checkBattery(DRONE_ID))
                                .willThrow(new ResourceNotFoundException("Drone", "ID", DRONE_ID));
                // WHEN
                this.mockMvc.perform(
                                get("/api/drones/" + DRONE_ID + "/battery"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()))
                                .andExpect(jsonPath("$.errorMessage")
                                                .value("Drone not found with the given input data ID: '" + DRONE_ID
                                                                + "'"));
        }

}
