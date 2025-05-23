package com.ruchij.dev.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.List;

public class FrontEndContainer extends GenericContainer<FrontEndContainer> {
    private static final int CONTAINER_PORT = 80;

    public FrontEndContainer() {
        super("ghcr.io/ruchira088/tic-tac-toe-front-end:main");
        setExposedPorts(List.of(CONTAINER_PORT));
        setWaitStrategy(Wait.forHttp("/").forPort(CONTAINER_PORT).forStatusCode(200));
    }

    public String getUrl() {
        return String.format("http://%s:%d", getHost(), getMappedPort(CONTAINER_PORT));
    }
}
