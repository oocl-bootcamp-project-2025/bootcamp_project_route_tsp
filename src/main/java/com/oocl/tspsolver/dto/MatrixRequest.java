package com.oocl.tspsolver.dto;

import com.oocl.tspsolver.entity.Point;

public class MatrixRequest {
    private Point[] points;
    private int type;

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
