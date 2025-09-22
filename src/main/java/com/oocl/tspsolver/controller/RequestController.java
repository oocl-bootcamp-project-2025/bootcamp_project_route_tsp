package com.oocl.tspsolver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.oocl.tspsolver.dto.MatrixRequest;
import com.oocl.tspsolver.service.TspSolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {
    @Autowired
    private TspSolverService tspSolverService;

    @PostMapping("/api/tsp/solver/distance")
    public JsonNode solveTspWithDistance(@RequestBody MatrixRequest matrixRequest) {
        return tspSolverService.solveTsp(matrixRequest.getPoints(), matrixRequest.getType());
    }
}
