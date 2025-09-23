package com.oocl.tspsolver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oocl.tspsolver.client.AmapClient;
import com.oocl.tspsolver.config.AmapProperties;
import com.oocl.tspsolver.entity.DistanceMatrix;
import com.oocl.tspsolver.entity.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class AmapApiService {
    private static final Logger logger = LoggerFactory.getLogger(AmapApiService.class);

    private final AmapProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    public AmapApiService(AmapProperties props) {
        this.props = props;
    }

    private String buildUrl(String originsParam, String destination, int type) {
        String encodedOrigins = URLEncoder.encode(originsParam, StandardCharsets.UTF_8);
        String encodedDestination = URLEncoder.encode(destination, StandardCharsets.UTF_8);

        return String.format("%s?origins=%s&destination=%s&type=%d&key=%s&output=json",
                props.getEndpoint(), encodedOrigins, encodedDestination, type, props.getApiKey());
    }

    public String formatter(Point p) {
        return String.format("%f,%f", p.getLongitude(), p.getLatitude());
    }

    public DistanceMatrix getDistanceMatrix(Point[] points, int type) {
        if (points == null || points.length == 0) {
            throw new IllegalArgumentException("Points array cannot be null or empty");
        }
        int n = points.length;
        DistanceMatrix distanceMatrix = new DistanceMatrix(n);

        for (int i = 0; i < n; i++) {
            distanceMatrix.set(i, i, 0.0);
        }

        int qpsDelayMs = props.getQpsDelayMs();
        int maxRetries = props.getMaxRetries();

        for (int i = 0; i < n; i++) {
            String destination = formatter(points[i]);
            for (int j = 0; j < n; j += 100) {
                List<Integer> originsIndex = new ArrayList<>();
                List<String> originsChunk = new ArrayList<>();
                for (int k = j; k < Math.min(j + 100, n); k++) {
                    if (k == i) {
                        continue;
                    }
                    originsIndex.add(k);
                    originsChunk.add(formatter(points[k]));
                }
                if (originsChunk.isEmpty()) {
                    continue;
                }
                String originsParam = originsChunk.stream().reduce((a, b) -> a + "|" + b).orElse("");

                String url = buildUrl(originsParam, destination, type);

                int attempt = 0;
                while (true) {
                    if (attempt > 0) {
                        // Exponential backoff
                        double backoff = Math.min(2000, 200*Math.pow(2, attempt - 1));
                        float jitter = (float) (Math.random() * 200);
                        try {
                            Thread.sleep((long) backoff + (int)jitter);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        try {
                            Thread.sleep(qpsDelayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    attempt++;
                    try {
                        String response = HttpService.sendGet(url);
                        if (response == null || response.isEmpty()) {
                            if (attempt >= maxRetries) {
                                throw new RuntimeException("Failed to get valid response after " + maxRetries + " attempts");
                            }
                            continue;
                        }
                        System.out.println(response);
                        JsonNode root = mapper.readTree(response);
                        if (root.path("status").asInt() != 1) {
                            if (attempt >= maxRetries) {
                                throw new RuntimeException("API error: " + root.path("info").asText());
                            }
                            continue;
                        }
                        JsonNode results = root.path("results");

                        for (int idx = 0; idx < results.size(); idx++) {
                            JsonNode result = results.get(idx);
                            int originIdx = originsIndex.get(idx);
                            double distance = result.path("distance").asDouble();
                            distanceMatrix.set(originIdx, i, distance);
                        }
                        break;
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return distanceMatrix;
    }
}
