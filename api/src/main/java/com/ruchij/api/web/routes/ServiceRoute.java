package com.ruchij.api.web.routes;

import com.ruchij.api.service.health.HealthService;
import com.ruchij.api.service.health.models.HealthCheck;
import com.ruchij.api.service.health.models.ServiceInformation;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.get;

public class ServiceRoute implements EndpointGroup {
    private final HealthService healthService;

    public ServiceRoute(HealthService healthService) {
        this.healthService = healthService;
    }

    @Override
    public void addEndpoints() {
        get("/info", context -> {
            ServiceInformation serviceInformation = healthService.serviceInformation();
            context.status(HttpStatus.OK).json(serviceInformation);
        });

        get("/health", context -> {
            HealthCheck healthCheck = healthService.healthCheck();

            context
                .status(healthCheck.isHealthy() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .json(healthCheck);
        });
    }
}
