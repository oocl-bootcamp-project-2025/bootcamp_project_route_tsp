package com.oocl.tspsolver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    private JsonNode convertToOpenPath(JsonNode loopSolution) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();

        result.put("status", loopSolution.get("status").asText());
        result.put("unit", loopSolution.get("unit").asText());

        // Find the most expensive leg
        ArrayNode legs = (ArrayNode) loopSolution.get("legs");
        int mostExpensiveIndex = 0;
        double maxCost = 0;

        for (int i = 0; i < legs.size(); i++) {
            double cost = legs.get(i).get("cost").asDouble();
            if (cost > maxCost) {
                maxCost = cost;
                mostExpensiveIndex = i;
            }
        }

        int toNode = legs.get(mostExpensiveIndex).get("toIndex").asInt();
        ArrayNode originalOrder = (ArrayNode) loopSolution.get("order");
        ArrayNode newOrder = mapper.createArrayNode();

        int startPos = -1;
        for (int i = 0; i < originalOrder.size() - 1; i++) {
            if (originalOrder.get(i).asInt() == toNode) {
                startPos = i;
                break;
            }
        }

        // THIS PART WAS MISSING: Populate newOrder starting from toNode
        for (int i = 0; i < originalOrder.size() - 1; i++) {
            int pos = (startPos + i) % (originalOrder.size() - 1);
            newOrder.add(originalOrder.get(pos));
        }

        ArrayNode newLegs = mapper.createArrayNode();
        double totalCost = 0;
        for (int i = 0; i < newOrder.size() - 1; i++) {
            int fromIndex = newOrder.get(i).asInt();
            int toIndex = newOrder.get(i + 1).asInt();
            // Find cost in original legs
            double legCost = 0;
            for (int j = 0; j < legs.size(); j++) {
                if (legs.get(j).get("fromIndex").asInt() == fromIndex &&
                        legs.get(j).get("toIndex").asInt() == toIndex) {
                    legCost = legs.get(j).get("cost").asDouble();
                    break;
                }
            }
            totalCost += legCost;
            ObjectNode leg = mapper.createObjectNode();
            leg.put("fromIndex", fromIndex);
            leg.put("toIndex", toIndex);
            leg.put("cost", legCost);
            leg.put("cumulativeCost", totalCost);
            newLegs.add(leg);
        }

        result.set("order", newOrder);
        result.set("legs", newLegs);
        result.put("objectiveCost", totalCost);
        result.put("startIndex", newOrder.get(0).asInt());
        result.put("endIndex", newOrder.get(newOrder.size() - 1).asInt());
        result.put("totalCost", totalCost);

        return result;
    }


    public JsonNode solveTsp(Point[] points, int type) {
        Loader.loadNativeLibraries();
        distanceMatrix = amapApiService.getDistanceMatrix(points, type);
        int n = points.length;

        // Optimization, try each waypoint as potential starting point
        double bestObjective = Double.MAX_VALUE;
        JsonNode bestResult = null;

        for (int startNode = 0; startNode < n; startNode++) {
            RoutingIndexManager manager = new RoutingIndexManager(n, 1, startNode);
            RoutingModel routing = getRoutingModel(manager);
            // No penality for open route
            routing.setFixedCostOfAllVehicles(0);

            RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                    .toBuilder()
                    .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                    .build();
            Assignment solution = routing.solveWithParameters(searchParameters);
            if (solution != null) {
                double objective = solution.objectiveValue();
                if (objective < bestObjective) {
                    bestObjective = objective;
                    bestResult = OrtoolsResultToJson.toJson(
                            routing,
                            manager,
                            solution,
                            type == 1 ? "seconds" : "meters",
                            null,
                            null);
                }
            }
        }
        return convertToOpenPath(bestResult);
    }

    public JsonNode solveTspOld(Point[] points, int type) {
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
