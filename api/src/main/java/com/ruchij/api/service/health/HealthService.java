package com.ruchij.api.service.health;

import com.ruchij.api.service.health.models.HealthCheck;
import com.ruchij.api.service.health.models.ServiceInformation;

public interface HealthService {
    ServiceInformation serviceInformation();

    HealthCheck healthCheck();
}