package com.oocl.tspsolver.model;

public class OrtoolsAssignmentSolution {
    private String status;
    private double objectiveCost;
    private String unit;
    private int[] order;
    private OrtoolsLegs[] legs;
    private int startIndex;
    private int endIndex;
    private double totalCost;

    public OrtoolsAssignmentSolution(String status, double objectiveCost, String unit, int[] order, OrtoolsLegs[] legs, int startIndex, int endIndex, double totalCost) {
        this.status = status;
        this.objectiveCost = objectiveCost;
        this.unit = unit;
        this.order = order;
        this.legs = legs;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getObjectiveCost() {
        return objectiveCost;
    }

    public void setObjectiveCost(double objectiveCost) {
        this.objectiveCost = objectiveCost;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int[] getOrder() {
        return order;
    }

    public void setOrder(int[] order) {
        this.order = order;
    }

    public OrtoolsLegs[] getLegs() {
        return legs;
    }

    public void setLegs(OrtoolsLegs[] legs) {
        this.legs = legs;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}
