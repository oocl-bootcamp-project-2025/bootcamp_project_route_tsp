package com.oocl.tspsolver.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.ortools.constraintsolver.*;

import java.util.ArrayList;
import java.util.List;

public final class OrtoolsResultToJson {

    public static class LonLat {
        public final double lon, lat;
        public LonLat(double lon, double lat) { this.lon = lon; this.lat = lat; }
    }

    /**
     * Convert an OR-Tools solution to JSON.
     *
     * @param routing  RoutingModel you solved.
     * @param manager  RoutingIndexManager used by the model.
     * @param solution Assignment returned by solveWithParameters (non-null).
     * @param unit     "seconds" if you used duration matrix; "meters" if you used distance matrix.
     * @param coords   Optional coordinates aligned with matrix rows (node index -> lon/lat). Can be null.
     * @param poiIds   Optional POI IDs aligned with matrix rows (node index -> your ID). Can be null.
     * @return JsonNode (ObjectNode) with route summary + legs + optional Amap fields.
     */
    public static JsonNode toJson(
            RoutingModel routing,
            RoutingIndexManager manager,
            Assignment solution,
            String unit,
            List<LonLat> coords,
            List<String> poiIds) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        // Status and objective
        root.put("status", routing.status().toString()); // e.g., ROUTING_SUCCESS
        root.put("objectiveCost", solution.objectiveValue());
        root.put("unit", unit);

        // Extract a single-vehicle route (vehicle 0)
        ArrayNode order = mapper.createArrayNode();
        ArrayNode legs = mapper.createArrayNode();

        long index = routing.start(0);
        long cumulative = 0;
        List<Integer> nodeOrder = new ArrayList<>();

        while (!routing.isEnd(index)) {
            int fromNode = manager.indexToNode(index);
            nodeOrder.add(fromNode);

            long nextIndex = solution.value(routing.nextVar(index));
            int toNode = manager.indexToNode(nextIndex);
            long arcCost = routing.getArcCostForVehicle(index, nextIndex, 0);
            cumulative += arcCost;

            ObjectNode leg = mapper.createObjectNode();
            leg.put("fromIndex", fromNode);
            leg.put("toIndex", toNode);
            leg.put("cost", arcCost);
            leg.put("cumulativeCost", cumulative);

            // Optional: attach POI id and coordinates for each leg
            if (poiIds != null && fromNode < poiIds.size()) leg.put("fromPoiId", poiIds.get(fromNode));
            if (poiIds != null && toNode < poiIds.size()) leg.put("toPoiId", poiIds.get(toNode));

            if (coords != null) {
                if (fromNode < coords.size()) {
                    ObjectNode from = mapper.createObjectNode();
                    from.put("lon", coords.get(fromNode).lon);
                    from.put("lat", coords.get(fromNode).lat);
                    leg.set("from", from);
                }
                if (toNode < coords.size()) {
                    ObjectNode to = mapper.createObjectNode();
                    to.put("lon", coords.get(toNode).lon);
                    to.put("lat", coords.get(toNode).lat);
                    leg.set("to", to);
                }
            }

            legs.add(leg);
            index = nextIndex;
        }
        // Add the final end node
        int endNode = manager.indexToNode(index);
        nodeOrder.add(endNode);

        // order array
        for (int node : nodeOrder) order.add(node);
        root.set("order", order);
        root.set("legs", legs);
        root.put("startIndex", nodeOrder.get(0));
        root.put("endIndex", endNode);
        root.put("totalCost", cumulative);

        // Optional: provide Amap-ready fields (origin/destination/waypoints)
        if (coords != null && !coords.isEmpty()) {
            String origin = fmt(coords.get(nodeOrder.get(0)));
            String destination = fmt(coords.get(endNode));
            String waypoints = buildWaypoints(coords, nodeOrder);

            ObjectNode amap = mapper.createObjectNode();
            amap.put("origin", origin);
            amap.put("destination", destination);
            amap.put("waypoints", waypoints); // pass to /v3/direction/driving
            root.set("amap", amap);
        }

        return root;
    }

    private static String fmt(LonLat p) {
        return p.lon + "," + p.lat; // Amap expects "lon,lat"
    }

    private static String buildWaypoints(List<LonLat> coords, List<Integer> order) {
        if (order.size() <= 2) return "";
        StringBuilder sb = new StringBuilder();
        for (int k = 1; k < order.size() - 1; k++) {
            if (k > 1) sb.append("|");
            LonLat p = coords.get(order.get(k));
            sb.append(fmt(p));
        }
        return sb.toString();
    }
}
