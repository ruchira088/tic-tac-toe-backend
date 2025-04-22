package com.ruchij;

import com.ruchij.config.ApplicationConfiguration;
import com.ruchij.service.health.HealthServiceImpl;
import com.ruchij.utils.JsonUtils;
import com.ruchij.web.Routes;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import java.io.IOException;
import java.time.Clock;
import java.util.Properties;

public class App {
    public static void main(String[] args) throws IOException {
        Config config = ConfigFactory.load();
        ApplicationConfiguration applicationConfiguration = ApplicationConfiguration.parse(config);

        Properties properties = System.getProperties();
        Clock clock = Clock.systemUTC();

        Routes routes = routes(applicationConfiguration, properties, clock);

        javalin(routes)
            .start(applicationConfiguration.httpConfiguration().port());
    }

    public static Javalin javalin(Routes routes) {
        return Javalin.create(javalinConfig -> {
              javalinConfig.jsonMapper(new JavalinJackson(JsonUtils.objectMapper, true));
              javalinConfig.router.apiBuilder(routes);
          });
    }

    private static Routes routes(ApplicationConfiguration applicationConfiguration, Properties properties, Clock clock)
        throws IOException {
        HealthServiceImpl healthService = HealthServiceImpl.create(clock, properties);

        return new Routes(healthService);
    }
}
