package com.ruchij.api;

import com.github.javafaker.Faker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.ruchij.api.config.ApplicationConfiguration;
import com.ruchij.api.dao.auth.AuthTokenDao;
import com.ruchij.api.dao.auth.MongoAuthTokenDaoImpl;
import com.ruchij.api.dao.game.GameDao;
import com.ruchij.api.dao.game.MongoGameDaoImpl;
import com.ruchij.api.dao.user.MongoUserDaoImpl;
import com.ruchij.api.dao.user.UserDao;
import com.ruchij.api.service.auth.AuthenticationService;
import com.ruchij.api.service.auth.AuthenticationServiceImpl;
import com.ruchij.api.service.game.GameEngine;
import com.ruchij.api.service.game.GameEngineImpl;
import com.ruchij.api.service.game.GameService;
import com.ruchij.api.service.game.GameServiceImpl;
import com.ruchij.api.service.hashing.BcryptPasswordHashingService;
import com.ruchij.api.service.hashing.PasswordHashingService;
import com.ruchij.api.service.health.HealthService;
import com.ruchij.api.service.health.HealthServiceImpl;
import com.ruchij.api.service.random.RandomGenerator;
import com.ruchij.api.service.random.RandomGeneratorImpl;
import com.ruchij.api.service.user.UserService;
import com.ruchij.api.service.user.UserServiceImpl;
import com.ruchij.api.utils.JsonUtils;
import com.ruchij.api.web.Routes;
import com.ruchij.api.web.middleware.ExceptionMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ApiApp {
    private static final Logger logger = LoggerFactory.getLogger(ApiApp.class);

    public static void main(String[] args) throws IOException {
        Config config = ConfigFactory.load();
        ApplicationConfiguration applicationConfiguration = ApplicationConfiguration.parse(config);

        run(applicationConfiguration);
    }

    public static void run(ApplicationConfiguration applicationConfiguration) throws IOException {
        Properties properties = System.getProperties();
        Clock clock = Clock.systemUTC();

        Routes routes = routes(applicationConfiguration, properties, clock);

        Javalin app = javalin(routes, applicationConfiguration.httpConfiguration().allowedOrigins());
        ExceptionMapper.handle(app);
        app.start(applicationConfiguration.httpConfiguration().port());

        logger.info("Server is listening on port {}...", applicationConfiguration.httpConfiguration().port());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            app.stop();
            logger.info("Server has been shut down.");
        }));
    }

    public static Javalin javalin(Routes routes, List<String> allowedOrigins) {
        return Javalin.create(javalinConfig -> {
            javalinConfig.useVirtualThreads = true;
            javalinConfig.jsonMapper(new JavalinJackson(JsonUtils.objectMapper, true));

            // wait 7 seconds for existing requests to finish
            javalinConfig.jetty.modifyServer(server -> server.setStopTimeout(7_000));

            javalinConfig.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> {
                    List<String> allAllowedOrigins = new ArrayList<>(allowedOrigins);
                    allAllowedOrigins.addAll(List.of(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "https://tic-tac-toe.home.ruchij.com"
                    ));

                    rule.allowHost(
                        "https://*.tic-tac-toe.home.ruchij.com",
                        allAllowedOrigins.toArray(String[]::new)
                    );

                    rule.allowCredentials = true;
                });
            });
            javalinConfig.router.apiBuilder(routes);
        });
    }

    private static Routes routes(
        ApplicationConfiguration applicationConfiguration,
        Properties properties,
        Clock clock
    )
        throws IOException {
        String mongoCollectionNamePrefix = applicationConfiguration.mongoConfiguration().collectionNameSuffix();
        MongoClient mongoClient = MongoClients.create(applicationConfiguration.mongoConfiguration().connectionUrl());
        MongoDatabase mongoDatabase = mongoClient.getDatabase(applicationConfiguration.mongoConfiguration().database());

        PasswordHashingService passwordHashingService = new BcryptPasswordHashingService();
        UserDao userDao = new MongoUserDaoImpl(mongoDatabase, mongoCollectionNamePrefix);
        Faker faker = Faker.instance();
        RandomGenerator randomGenerator = new RandomGeneratorImpl(userDao, faker);

        UserService userService = new UserServiceImpl(userDao, passwordHashingService, randomGenerator, clock);

        GameDao gameDao = new MongoGameDaoImpl(mongoDatabase, mongoCollectionNamePrefix);
        GameEngine gameEngine = new GameEngineImpl();
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        GameService gameService = new GameServiceImpl(gameDao, gameEngine, executorService, clock, randomGenerator);

        AuthTokenDao authTokenDao = new MongoAuthTokenDaoImpl(mongoDatabase, mongoCollectionNamePrefix);
        AuthenticationService authenticationService =
            new AuthenticationServiceImpl(userDao, authTokenDao, passwordHashingService, randomGenerator, clock);

        HealthService healthService = HealthServiceImpl.create(mongoDatabase, clock, properties);

        ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(1000, Thread.ofVirtual().factory());

        return new Routes(
            userService,
            gameService,
            authenticationService,
            healthService,
            scheduledExecutorService,
            clock
        );
    }
}
