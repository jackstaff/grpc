package io.grpc.examples.routeguide;

/**
 * Generated by org.jackstaff.grpc.generator.ProtocolProcessor;  DO NOT EDIT!
 */
public class RouteSummary {
    private int pointCount;

    private int featureCount;

    private int distance;

    private int elapsedTime;

    public RouteSummary() {
    }

    public RouteSummary(int pointCount, int featureCount, int distance, int elapsedTime) {
        this.pointCount = pointCount;
        this.featureCount = featureCount;
        this.distance = distance;
        this.elapsedTime = elapsedTime;
    }

    public int getPointCount() {
        return this.pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public int getFeatureCount() {
        return this.featureCount;
    }

    public void setFeatureCount(int featureCount) {
        this.featureCount = featureCount;
    }

    public int getDistance() {
        return this.distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getElapsedTime() {
        return this.elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}