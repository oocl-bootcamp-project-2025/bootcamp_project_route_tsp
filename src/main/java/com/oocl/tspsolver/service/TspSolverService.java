package com.oocl.tspsolver.service;

import com.oocl.tspsolver.entity.DistanceMatrix;
import com.oocl.tspsolver.entity.Point;
import com.oocl.tspsolver.utils.OrtoolsResultToJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;


@Service
public class TspSolverService {
    @Autowired
    private AmapApiService amapApiService;

    private DistanceMatrix distanceMatrix;

    public JsonNode solveTsp(Point[] points, int type) {
        Loader.loadNativeLibraries();
        distanceMatrix = amapApiService.getDistanceMatrix(points, type);
        int n = points.length;

        // Create the routing index manager
        RoutingIndexManager manager = new RoutingIndexManager(n, 1, 0);

        // Create Routing Model
        RoutingModel routing = getRoutingModel(manager);

        // Setting the first solution heuristic
        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters()
                        .toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PARALLEL_CHEAPEST_INSERTION)
                        .build();

        // Solve the problem
        Assignment solution = routing.solveWithParameters(searchParameters);

        return OrtoolsResultToJson.toJson(routing, manager, solution, type == 1 ? "seconds" : "meters", null, null);
    }

    private RoutingModel getRoutingModel(RoutingIndexManager manager) {
        RoutingModel routing = new RoutingModel(manager);

        // Create and register a transit callback
        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            // Convert from routing variable Index to distance matrix NodeIndex
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return Math.round(distanceMatrix.get(fromNode, toNode));
        });

        // Define cost of each arc
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);
        return routing;
    }
}
