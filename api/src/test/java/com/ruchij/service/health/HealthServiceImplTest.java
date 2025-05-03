package com.ruchij.service.health;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.ruchij.service.health.models.BuildInformation;
import com.ruchij.service.health.models.HealthCheck;
import com.ruchij.service.health.models.ServiceInformation;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class HealthServiceImplTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2025-04-15T10:15:30Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.of("UTC"));
    private static final String DATABASE_NAME = "test-db";

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER =
        new MongoDBContainer("mongo:8");

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private ExecutorService executorService;
    private Properties properties;
    private BuildInformation buildInformation;
    private HealthServiceImpl healthService;

    @BeforeAll
    static void beforeAll() {
        MONGO_DB_CONTAINER.start();
    }

    @AfterAll
    static void afterAll() {
        MONGO_DB_CONTAINER.stop();
    }

    @BeforeEach
    void setUp() {
        mongoClient = MongoClients.create(MONGO_DB_CONTAINER.getConnectionString());
        mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        
        properties = new Properties();
        properties.setProperty("java.version", "21");
        
        buildInformation = new BuildInformation(
            "test-service",
            "com.ruchij",
            "1.0.0",
            "8.5",
            Instant.parse("2023-08-10T00:00:00Z"),
            "main",
            "abc123"
        );
        
        healthService = new HealthServiceImpl(
            mongoDatabase,
            executorService,
            FIXED_CLOCK,
            properties,
            buildInformation
        );
    }

    @AfterEach
    void tearDown() {
        mongoClient.close();
        executorService.shutdownNow();
    }

    @Test
    void serviceInformationShouldReturnCorrectInformation() {
        ServiceInformation result = healthService.serviceInformation();
        
        assertEquals("test-service", result.serviceName());
        assertEquals("1.0.0", result.serviceVersion());
        assertEquals("21", result.javaVersion());
        assertEquals("8.5", result.gradleVersion());
        assertEquals(FIXED_INSTANT, result.currentTimestamp());
        assertEquals("main", result.gitBranch());
        assertEquals("abc123", result.gitCommit());
        assertEquals(Instant.parse("2023-08-10T00:00:00Z"), result.buildTimestamp());
    }

    @Test
    void healthCheckShouldReturnHealthyWhenDatabaseIsUp() {
        // Using the real MongoDB container
        HealthCheck result = healthService.healthCheck();
        
        assertEquals(HealthCheck.Status.Healthy, result.database());
        assertTrue(result.isHealthy());
    }

    @Test
    void healthCheckShouldReturnUnhealthyWhenDatabaseIsDown() {
        // Create a client with invalid connection string to simulate database being down
        MongoClient invalidClient = MongoClients.create("mongodb://localhost:27999");
        MongoDatabase invalidDatabase = invalidClient.getDatabase("invalid-db");
        
        HealthServiceImpl unhealthyService = new HealthServiceImpl(
            invalidDatabase,
            executorService,
            FIXED_CLOCK,
            properties,
            buildInformation
        );
        
        // Test with invalid database connection
        HealthCheck result = unhealthyService.healthCheck();
        
        assertEquals(HealthCheck.Status.Unhealthy, result.database());
        assertFalse(result.isHealthy());
        
        invalidClient.close();
    }

    @Test
    void healthCheckShouldHandleTimeout() {
        // Create a HealthService with a small timeout by using a real database but with a test that completes quickly
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        
        // This executor service will be busy with another task
        singleThreadExecutor.submit(() -> {
            try {
                // Block the executor for longer than the timeout in the healthCheck method
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // Ignore
            }
        });
        
        HealthServiceImpl timeoutService = new HealthServiceImpl(
            mongoDatabase,
            singleThreadExecutor, // Use the busy executor service
            FIXED_CLOCK,
            properties,
            buildInformation
        );
        
        // The health check should timeout as the executor is busy
        HealthCheck result = timeoutService.healthCheck();
        
        assertEquals(HealthCheck.Status.Unhealthy, result.database());
        assertFalse(result.isHealthy());
        
        singleThreadExecutor.shutdownNow();
    }
}
