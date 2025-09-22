package com.oocl.tspsolver.model;

public class OrtoolsLegs {
    private int fromIndex;
    private int toIndex;
    private double cost;
    private double cumulativeCost;

    public OrtoolsLegs(int fromIndex, int toIndex, double cost, double cumulativeCost) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.cost = cost;
        this.cumulativeCost = cumulativeCost;
    }

    public double getCumulativeCost() {
        return cumulativeCost;
    }

    public void setCumulativeCost(double cumulativeCost) {
        this.cumulativeCost = cumulativeCost;
    }

    public int getToIndex() {
        return toIndex;
    }

    public void setToIndex(int toIndex) {
        this.toIndex = toIndex;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
