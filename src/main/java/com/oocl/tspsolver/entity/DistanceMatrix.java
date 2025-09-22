package com.oocl.tspsolver.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class DistanceMatrix {
    private final double[][] matrix;

    public double[][] getDistances() {
        return matrix;
    }

    public DistanceMatrix(int n) {
        this.matrix = new double[n][n];
        for (int i = 0 ; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.matrix[i][j] = i == j ? 0.0 : -1.0;
            }
        }
    }

    public int size() {
        return matrix.length;
    }

    public void set(int i, int j, double value) {
        matrix[i][j] = value;
    }

    public double get(int i, int j) {
        return matrix[i][j];
    }

    public double[][] getMatrix() {
        return matrix;
    }

    public JsonNode toJsonNode(ObjectMapper mapper) {
        ArrayNode root = mapper.createArrayNode();
        for (double[] row : matrix) {
            ArrayNode rowNode = mapper.createArrayNode();
            for (double val : row) {
                rowNode.add(val);
            }
            root.add(rowNode);
        }
        return root;
    }
}
