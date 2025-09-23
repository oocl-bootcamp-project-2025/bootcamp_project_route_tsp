package com.oocl.tspsolver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.oocl.tspsolver.dto.MatrixRequest;
import com.oocl.tspsolver.entity.Point;
import com.oocl.tspsolver.service.RoutePlannerService;
import com.oocl.tspsolver.service.TspSolverService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.*;

import java.util.Arrays;

@RestController
public class RequestController {
    private static final int MAX_POINTS = 100;

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    @Autowired
    private RoutePlannerService routePlannerService;

    @Autowired
    private TspSolverService tspSolverService;

    @GetMapping("/api/health")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping("/api/tsp/solver/distance")
    public ResponseEntity<JsonNode> solveTspWithDistance(
            @RequestBody MatrixRequest matrixRequest
    ) {
        try {
            // Validate request
            if (matrixRequest.getPoints() == null || matrixRequest.getPoints().length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            if (matrixRequest.getPoints().length > MAX_POINTS) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Validate coordinates
            for (Point point : matrixRequest.getPoints()) {
                if (point.getLongitude() == null || point.getLatitude() == null ||
                    point.getLongitude() < -180 || point.getLongitude() > 180 ||
                    point.getLatitude() < -90 || point.getLatitude() > 90) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }

            // Set type to 0 for distance if not specified
            if (matrixRequest.getType() != 1) {
                matrixRequest.setType(0);
            }

            JsonNode result = tspSolverService.solveTsp(matrixRequest.getPoints(), matrixRequest.getType());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/api/routePlanner")
    public ResponseEntity<JsonNode> getOptimizedRoute(@RequestBody MatrixRequest matrixRequest) {
        try {
            // Validate request
            if (matrixRequest.getPoints() == null || matrixRequest.getPoints().length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            if (matrixRequest.getPoints().length > MAX_POINTS) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Validate coordinates
            for (Point point : matrixRequest.getPoints()) {
                if (point.getLongitude() == null || point.getLatitude() == null ||
                    point.getLongitude() < -180 || point.getLongitude() > 180 ||
                    point.getLatitude() < -90 || point.getLatitude() > 90) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }

            JsonNode optimizedRoute = routePlannerService.getOptimizedRoute(matrixRequest.getPoints(),
                    matrixRequest.getType());
            return ResponseEntity.ok(optimizedRoute);
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
