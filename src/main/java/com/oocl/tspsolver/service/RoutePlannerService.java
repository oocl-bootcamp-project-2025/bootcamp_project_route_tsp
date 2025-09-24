package com.oocl.tspsolver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oocl.tspsolver.config.AmapProperties;
import com.oocl.tspsolver.entity.Point;
import com.oocl.tspsolver.exception.AmapDrivingApiException;
import com.oocl.tspsolver.exception.EmptyOptimizedOrderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
public class RoutePlannerService {
    private static final Logger logger = LoggerFactory.getLogger(RoutePlannerService.class);

    private final AmapProperties props;

    @Autowired
    private TspSolverService tspSolverService;

    public RoutePlannerService(AmapProperties props) {
        this.props = props;
    }

    private String buildRequestBody(Point[] waypoints) {
        String startPoint = URLEncoder.encode(
                String.format("%f,%f", waypoints[0].getLongitude(), waypoints[0].getLatitude()),
                StandardCharsets.UTF_8);
        String endPoint = URLEncoder.encode(
                String.format("%f,%f", waypoints[waypoints.length - 1].getLongitude(), waypoints[waypoints.length - 1].getLatitude()),
                StandardCharsets.UTF_8);
        StringBuilder waypointsParam = new StringBuilder();
        for (int i = 1; i < waypoints.length - 1; i++) {
            waypointsParam.append(String.format("%f,%f", waypoints[i].getLongitude(), waypoints[i].getLatitude()));
            if (i < waypoints.length - 2) {
                waypointsParam.append(";");
            }
        }
        String waypointsStr = URLEncoder.encode(waypointsParam.toString(), StandardCharsets.UTF_8);
        return """
                {
                    "origin": "%s",
                    "destination": "%s",
                    "waypoints": "%s",
                    "extensions": "all",
                    "strategy": 32,
                    "output": "json",
                    "key": "%s",
                    "show_fields": "cost,polyline"
                }
                """.formatted(startPoint, endPoint, waypointsStr, props.getApiKey());
    }
    private String buildUrl(Point origin, Point destination, Point[] points) {
        String startPoint = URLEncoder.encode(origin.getLongitude() + "," + origin.getLatitude(),
                StandardCharsets.UTF_8);
        String endPoint = URLEncoder.encode(destination.getLongitude() + "," + destination.getLatitude(),
                StandardCharsets.UTF_8);
        String waypoints = URLEncoder.encode(Arrays.stream(points).map(point ->
            String.format("%f,%f", point.getLongitude(), point.getLatitude())
        ).reduce((a, b) -> a + "|" + b).orElse(""), StandardCharsets.UTF_8);
        // Amap default
        int type = 32;
        return String.format("%s?origin=%s&destination=%s&key=%s&type=%d&waypoints=%s&output=json&extensions=all&strategy=32&show_fields=cost,polyline",
                props.getDrivingRouteEndpoint(), startPoint, endPoint, props.getApiKey(),
                type, waypoints);
    }

    public JsonNode getOptimizedRoute(Point[] points, int type) {
        logger.info("Starting route optimization for {} points", points.length);

        JsonNode tspResult = tspSolverService.solveTsp(points, type);

        logger.info("TSP Result: {}", tspResult.toString());

        if (tspResult == null || !tspResult.has("order")) {
            logger.error("TSP result is null or does not contain 'order' field");
            throw new EmptyOptimizedOrderException("TSP result is null or does not contain 'order' field");
        }
        Point[] optimizedOrder = new Point[points.length];
        for (int i = 0; i < optimizedOrder.length; i++) {
            int index = tspResult.get("order").get(i).asInt();
            optimizedOrder[i] = points[index];
        }

        String url = buildUrl(optimizedOrder[0], optimizedOrder[optimizedOrder.length - 1],
                Arrays.copyOfRange(optimizedOrder, 1, optimizedOrder.length - 1));
        logger.info("Amap Driving Route URL: {}", url);
        try {
            String response = HttpService.sendGet(url);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(response);
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new AmapDrivingApiException("Failed to get driving route from Amap API");
        }
    }

}
