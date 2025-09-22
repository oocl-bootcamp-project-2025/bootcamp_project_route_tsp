package com.oocl.tspsolver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oocl.tspsolver.entity.Point;
import com.oocl.tspsolver.service.TspSolverService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class TspSolverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TspSolverService tspSolverService;

    @Autowired
    private RequestController requestController;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSolveTspWithDistance() throws Exception {
        // Given
        Point[] points = {
                new Point(116.397128, 39.916527),
                new Point(116.321286, 39.896026),
                new Point(116.464595, 39.887329)
        };

        JsonNode mockResult = objectMapper.readTree("{\"route\":[0,1,2,0],\"distance\":25000}");
        when(tspSolverService.solveTsp(any(Point[].class), eq(0))).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/tsp/solver/distance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(points)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").isArray())
                .andExpect(jsonPath("$.distance").isNumber());
    }

//    @Test
//    public void testSolveTspWithDuration() throws Exception {
//        // Given
//        Point[] points = {
//                new Point(116.397128, 39.916527),
//                new Point(116.321286, 39.896026),
//                new Point(116.464595, 39.887329)
//        };
//
//        JsonNode mockResult = objectMapper.readTree("{\"route\":[0,1,2,0],\"duration\":1800}");
//        when(tspSolverService.solveTsp(any(Point[].class), eq(1))).thenReturn(mockResult);
//
//        // When & Then
//        mockMvc.perform(post("/api/tsp/solve/duration")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(points)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.route").isArray())
//                .andExpect(jsonPath("$.duration").isNumber());
//    }

    @Test
    public void testSolveTspWithEmptyPoints() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tsp/solver/distance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSolveTspWithInvalidPoints() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tsp/solver/distance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"lat\":null,\"lng\":null}]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSolveTspWithSinglePoint() throws Exception {
        // Given
        Point[] points = {
                new Point(116.397128, 39.916527)
        };

        JsonNode mockResult = objectMapper.readTree("{\"route\":[0,0],\"distance\":0}");
        when(tspSolverService.solveTsp(any(Point[].class), eq(0))).thenReturn(mockResult);

        // When & Then
        mockMvc.perform(post("/api/tsp/solver/distance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(points)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").isArray())
                .andExpect(jsonPath("$.route.length()").value(2))
                .andExpect(jsonPath("$.distance").value(0));
    }

    @Test
    public void testSolveTspWithTooManyPoints() throws Exception {
        // Given
        Point[] points = new Point[101]; // Assuming 100 is max limit
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(116.0 + i * 0.01, 39.0 + i * 0.01);
        }

        // When & Then
        mockMvc.perform(post("/api/tsp/solver/distance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(points)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testSolveTspServiceError() throws Exception {
        // Given
        Point[] points = {
                new Point(116.397128, 39.916527),
                new Point(116.321286, 39.896026)
        };

        when(tspSolverService.solveTsp(any(Point[].class), eq(0)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/tsp/solver/distance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(points)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testSolveTspWithInvalidCoordinates() throws Exception {
        // Given
        Point[] points = {
                new Point(-200, 39.916527), // Invalid longitude
                new Point(116.321286, 100)  // Invalid latitude
        };

        // When & Then
        mockMvc.perform(post("/api/tsp/solver/distance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(points)))
                .andExpect(status().isBadRequest());
    }
}
