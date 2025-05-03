package com.ruchij.service.health;

import com.mongodb.client.MongoDatabase;
import com.ruchij.service.health.models.BuildInformation;
import com.ruchij.service.health.models.HealthCheck;
import com.ruchij.service.health.models.ServiceInformation;
import com.ruchij.utils.JsonUtils;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HealthServiceImpl implements HealthService {
    private final MongoDatabase mongoDatabase;
    private final ExecutorService executorService;
    private final Clock clock;
    private final Properties properties;
    private final BuildInformation buildInformation;

    public HealthServiceImpl(
        MongoDatabase mongoDatabase,
        ExecutorService executorService,
        Clock clock,
        Properties properties,
        BuildInformation buildInformation
    ) {
        this.mongoDatabase = mongoDatabase;
        this.executorService = executorService;
        this.clock = clock;
        this.properties = properties;
        this.buildInformation = buildInformation;
    }

    public static HealthServiceImpl create(MongoDatabase mongoDatabase, Clock clock, Properties properties) throws IOException {
        InputStream inputStream = HealthServiceImpl.class.getClassLoader().getResourceAsStream("build-information.json");
        BuildInformation buildInformation;

        if (inputStream == null) {
            buildInformation = new BuildInformation(
                "tic-tac-toe-backend",
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

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        return new HealthServiceImpl(mongoDatabase, executorService, clock, properties, buildInformation);
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

    @Override
    public HealthCheck healthCheck() {
        CompletableFuture<HealthCheck.Status> databaseHealthCheckFuture =
            CompletableFuture.supplyAsync(this::databaseHealthCheck, this.executorService);

        HealthCheck.Status databaseHealthStatus = databaseHealthCheckFuture
            .completeOnTimeout(HealthCheck.Status.Unhealthy, 5, TimeUnit.SECONDS)
            .join();

        return new HealthCheck(databaseHealthStatus);
    }

    private HealthCheck.Status databaseHealthCheck() {
        try {
            Document ping = this.mongoDatabase.runCommand(new Document("ping", 1));

            if (ping.getDouble("ok") == 1.0) {
                return HealthCheck.Status.Healthy;
            } else {
                return HealthCheck.Status.Unhealthy;
            }
        } catch (Exception __) {
            return HealthCheck.Status.Unhealthy;
        }
    }
}
