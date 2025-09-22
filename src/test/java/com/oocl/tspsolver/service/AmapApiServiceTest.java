package com.oocl.tspsolver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oocl.tspsolver.client.AmapClient;
import com.oocl.tspsolver.config.AmapProperties;
import com.oocl.tspsolver.entity.DistanceMatrix;
import com.oocl.tspsolver.entity.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmapApiServiceTest {
    @Mock
    private AmapClient amapClient;

    @Mock
    private AmapProperties props;

    @InjectMocks
    private AmapApiService amapApiService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void formatter_should_return_correct_format() {
        Point point = new Point(116.481028, 39.989643);
        String result = amapApiService.formatter(point);
        assertEquals("116.481028,39.989643", result);
    }

    @Test
    void getDistanceMatrix_should_throw_exception_for_null_points() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> amapApiService.getDistanceMatrix(null, 1)
        );
        assertEquals("Points array cannot be null or empty", exception.getMessage());
    }

    @Test
    void getDistanceMatrix_should_throw_exception_for_empty_points() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> amapApiService.getDistanceMatrix(new Point[0], 1)
        );
        assertEquals("Points array cannot be null or empty", exception.getMessage());
    }

    @Test
    void getDistanceMatrix_should_handle_single_point() {
        // Setup
        Point[] points = new Point[] { new Point(116.481028, 39.989643) };

        // Act
        DistanceMatrix result = amapApiService.getDistanceMatrix(points, 1);

        // Assert
        assertEquals(1, result.getMatrix().length);
        assertEquals(0.0, result.get(0, 0));
    }

    @Test
    void getDistanceMatrix_should_build_matrix_from_api_response() throws Exception {
        // Setup
        Point[] points = new Point[] {
                new Point(116.481028, 39.989643),
                new Point(116.434446, 39.90816)
        };

        // Create successful response for both directions
        String successResponse = createSuccessResponse(10500.0);

        try (MockedStatic<HttpService> mockedHttpService = mockStatic(HttpService.class)) {
            // Configure to always return success for any URL
            mockedHttpService.when(() -> HttpService.sendGet(anyString()))
                    .thenReturn(successResponse);

            // Act
            DistanceMatrix matrix = amapApiService.getDistanceMatrix(points, 1);

            // Assert
            double[][] result = matrix.getMatrix();
            assertEquals(0.0, result[0][0]);
            assertEquals(0.0, result[1][1]);
            assertEquals(10500.0, result[1][0]);
            assertEquals(10500.0, result[0][1]);
        }
    }

    @Test
    void getDistanceMatrix_should_throw_exception_after_max_retries() throws Exception {
        // Setup
        Point[] points = new Point[] {
                new Point(116.481028, 39.989643),
                new Point(116.434446, 39.90816)
        };

        // Set max retries to 1
        when(props.getMaxRetries()).thenReturn(1);

        // Create error response
        String errorResponse = createErrorResponse("Error occurred");

        try (MockedStatic<HttpService> mockedHttpService = mockStatic(HttpService.class)) {
            // Always return error
            mockedHttpService.when(() -> HttpService.sendGet(anyString()))
                    .thenReturn(errorResponse);

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> amapApiService.getDistanceMatrix(points, 1)
            );

            assertThat(exception.getMessage()).contains("API error: Error occurred");
        }
    }

    private String createSuccessResponse(double distance) throws Exception {
        ObjectNode responseNode = JsonNodeFactory.instance.objectNode();
        responseNode.put("status", 1);

        ArrayNode resultsNode = JsonNodeFactory.instance.arrayNode();
        ObjectNode resultNode = JsonNodeFactory.instance.objectNode();
        resultNode.put("distance", distance);
        resultsNode.add(resultNode);

        responseNode.set("results", resultsNode);
        return objectMapper.writeValueAsString(responseNode);
    }

    private String createErrorResponse(String errorMessage) throws Exception {
        ObjectNode errorNode = JsonNodeFactory.instance.objectNode();
        errorNode.put("status", 0);
        errorNode.put("info", errorMessage);
        return objectMapper.writeValueAsString(errorNode);
    }
}
