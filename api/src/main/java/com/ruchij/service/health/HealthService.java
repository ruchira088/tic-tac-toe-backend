package com.ruchij.service.health;

import com.ruchij.service.health.models.HealthCheck;
import com.ruchij.service.health.models.ServiceInformation;

public interface HealthService {
    ServiceInformation serviceInformation();

    HealthCheck healthCheck();
}