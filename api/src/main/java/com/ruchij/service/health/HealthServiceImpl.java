package com.ruchij.service.health;

import com.ruchij.service.health.models.BuildInformation;
import com.ruchij.service.health.models.ServiceInformation;
import com.ruchij.utils.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Properties;

public class HealthServiceImpl implements HealthService {
    private final Clock clock;
    private final Properties properties;
    private final BuildInformation buildInformation;

    public HealthServiceImpl(Clock clock, Properties properties, BuildInformation buildInformation) {
        this.clock = clock;
        this.properties = properties;
        this.buildInformation = buildInformation;
    }

    @Override
    public ServiceInformation serviceInformation() {
        String javaVersion = properties.getProperty("java.version", "unknown");
        Instant timestamp = clock.instant();

        ServiceInformation serviceInformation = new ServiceInformation(
            buildInformation.name(),
            buildInformation.version(),
            javaVersion,
            buildInformation.gradleVersion(),
            timestamp,
            buildInformation.gitBranch(),
            buildInformation.gitCommit(),
            buildInformation.buildTimestamp()
        );

        return serviceInformation;
    }

    public static HealthServiceImpl create(Clock clock, Properties properties) throws IOException {
        InputStream inputStream = HealthServiceImpl.class.getClassLoader().getResourceAsStream("build-information.json");
        BuildInformation buildInformation;

        if (inputStream == null) {
            buildInformation = new BuildInformation(
                    "javalin-seed",
                    "com.ruchij",
                    "UNKNOWN",
                    "UNKNOWN",
                    null,
                    "UNKNOWN",
                    "UNKNOWN"

            );
        } else {
            buildInformation = JsonUtils.objectMapper.readValue(inputStream, BuildInformation.class);
        }

        return new HealthServiceImpl(clock, properties, buildInformation);
    }
}
