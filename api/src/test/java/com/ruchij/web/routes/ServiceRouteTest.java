package com.ruchij.web.routes;

import com.ruchij.App;
import com.ruchij.service.game.GameService;
import com.ruchij.service.health.HealthService;
import com.ruchij.service.health.models.ServiceInformation;
import com.ruchij.service.user.UserService;
import com.ruchij.web.Routes;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;

import static com.ruchij.utils.JsonUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceRouteTest {

    @Test
    void shouldReturnServiceInformation() {
        UserService userService = Mockito.mock(UserService.class);
        GameService gameService = Mockito.mock(GameService.class);
        HealthService healthService = Mockito.mock(HealthService.class);

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

        Routes routes = new Routes(userService, gameService, healthService);

        JavalinTest.test(App.javalin(routes), ((server, client) -> {
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