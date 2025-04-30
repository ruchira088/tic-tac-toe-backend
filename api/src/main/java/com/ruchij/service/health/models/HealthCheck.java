package com.ruchij.service.health.models;

public record HealthCheck(Status database) {
    public boolean isHealthy() {
        return database == Status.Healthy;
    }

    public enum Status {
        Healthy, Unhealthy
    }
}
