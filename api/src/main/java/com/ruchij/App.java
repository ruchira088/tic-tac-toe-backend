package com.ruchij;

import com.github.javafaker.Faker;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.ruchij.config.ApplicationConfiguration;
import com.ruchij.dao.auth.AuthTokenDao;
import com.ruchij.dao.auth.MongoAuthTokenDaoImpl;
import com.ruchij.dao.game.GameDao;
import com.ruchij.dao.game.MongoGameDaoImpl;
import com.ruchij.dao.user.MongoUserDaoImpl;
import com.ruchij.dao.user.UserDao;
import com.ruchij.service.auth.AuthenticationService;
import com.ruchij.service.auth.AuthenticationServiceImpl;
import com.ruchij.service.game.GameEngine;
import com.ruchij.service.game.GameEngineImpl;
import com.ruchij.service.game.GameService;
import com.ruchij.service.game.GameServiceImpl;
import com.ruchij.service.hashing.BcryptPasswordHashingService;
import com.ruchij.service.hashing.PasswordHashingService;
import com.ruchij.service.health.HealthService;
import com.ruchij.service.health.HealthServiceImpl;
import com.ruchij.service.random.RandomGenerator;
import com.ruchij.service.random.RandomGeneratorImpl;
import com.ruchij.service.user.UserService;
import com.ruchij.service.user.UserServiceImpl;
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
            javalinConfig.useVirtualThreads = true;
            javalinConfig.jsonMapper(new JavalinJackson(JsonUtils.objectMapper, true));
            javalinConfig.router.apiBuilder(routes);
        });
    }

    private static Routes routes(
            ApplicationConfiguration applicationConfiguration,
            Properties properties,
            Clock clock
    )
            throws IOException {
        MongoClient mongoClient = MongoClients.create(applicationConfiguration.mongoConfiguration().connectionUrl());
        MongoDatabase mongoDatabase = mongoClient.getDatabase(applicationConfiguration.mongoConfiguration().database());

        PasswordHashingService passwordHashingService = new BcryptPasswordHashingService();
        UserDao userDao = new MongoUserDaoImpl(mongoDatabase);
        Faker faker = Faker.instance();
        RandomGenerator randomGenerator = new RandomGeneratorImpl(userDao, faker);

        UserService userService = new UserServiceImpl(userDao, passwordHashingService, randomGenerator, clock);

        GameDao gameDao = new MongoGameDaoImpl(mongoDatabase);
        GameEngine gameEngine = new GameEngineImpl();
        GameService gameService = new GameServiceImpl(gameDao, gameEngine, clock, randomGenerator);

        AuthTokenDao authTokenDao = new MongoAuthTokenDaoImpl(mongoDatabase);
        AuthenticationService authenticationService =
            new AuthenticationServiceImpl(userDao, authTokenDao, passwordHashingService, randomGenerator, clock);

        HealthService healthService = HealthServiceImpl.create(mongoDatabase, clock, properties);

        return new Routes(userService, gameService, authenticationService, healthService);
    }
}
