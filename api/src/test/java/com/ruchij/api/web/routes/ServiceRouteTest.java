package com.ruchij.api.web.routes;

import com.ruchij.api.ApiApp;
import com.ruchij.api.service.auth.AuthenticationService;
import com.ruchij.api.service.game.GameService;
import com.ruchij.api.service.health.HealthService;
import com.ruchij.api.service.health.models.ServiceInformation;
import com.ruchij.api.service.user.UserService;
import com.ruchij.api.web.Routes;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static com.ruchij.api.utils.JsonUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceRouteTest {

    @Test
    void shouldReturnServiceInformation() {
        UserService userService = Mockito.mock(UserService.class);
        GameService gameService = Mockito.mock(GameService.class);
        AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);
        HealthService healthService = Mockito.mock(HealthService.class);
        ScheduledExecutorService scheduledExecutorService = Mockito.mock(ScheduledExecutorService.class);
        Clock clock = Mockito.mock(Clock.class);

        Instant timestamp = Instant.parse("2023-02-05T04:37:42.566735Z");

        Mockito.when(healthService.serviceInformation())
            .thenReturn(new ServiceInformation(
                "tic-tac-toe-backend",
                "0.0.1-SNAPSHOT",
                "17.0.5",
                "7.6",
                timestamp,
                "main",
                "my-commit",
                timestamp
            ));

        Routes routes = new Routes(
            userService,
            gameService,
            authenticationService,
            healthService,
            scheduledExecutorService,
            clock
        );

        JavalinTest.test(ApiApp.javalin(routes, List.of()), ((server, client) -> {
            Response response = client.get("/service/info");
            assertEquals(200, response.code());

            String expectedResponseBody = """
                {
                    "serviceName": "tic-tac-toe-backend",
                    "serviceVersion": "0.0.1-SNAPSHOT",
                    "javaVersion": "17.0.5",
                    "gradleVersion": "7.6",
                    "currentTimestamp": "2023-02-05T04:37:42.566735Z",
                    "gitBranch": "main",
                    "gitCommit": "my-commit",
                    "buildTimestamp": "2023-02-05T04:37:42.566735Z"
                 }
                """;

            assertEquals(
                objectMapper.readTree(expectedResponseBody),
                objectMapper.readTree(response.body().byteStream())
            );
        }));
    }

}