package com.oocl.tspsolver.entity;

public class DistanceMatrix {
    private final double[][] matrix;

    public DistanceMatrix(int size) {
        matrix = new double[size][size];
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
}
