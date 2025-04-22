package com.ruchij.web.routes;

import com.ruchij.service.health.HealthService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import java.util.concurrent.CompletableFuture;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class ServiceRoute implements EndpointGroup {
    private final HealthService healthService;

    public ServiceRoute(HealthService healthService) {
        this.healthService = healthService;
    }

    @Override
    public void addEndpoints() {
        path("/info", () ->
            get(context -> context.future(() ->
                    CompletableFuture.supplyAsync(healthService::serviceInformation)
                        .thenAccept(serviceInformation ->
                            context
                                .status(HttpStatus.OK)
                                .json(serviceInformation)
                        )
                )
            )
        );
    }
}
