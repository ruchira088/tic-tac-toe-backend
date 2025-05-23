package com.ruchij.dev;

import com.ruchij.api.ApiApp;
import com.ruchij.api.config.ApplicationConfiguration;
import com.ruchij.api.config.HttpConfiguration;
import com.ruchij.api.config.MongoConfiguration;
import com.ruchij.dev.container.FrontEndContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;

import java.io.IOException;
import java.util.List;

public class DevApp {
    private static final Logger logger = LoggerFactory.getLogger(DevApp.class);

    public static void main(String[] args) throws IOException {
        MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8");
        mongoDBContainer.start();
        String mongoConnectionUrl = mongoDBContainer.getConnectionString();

        FrontEndContainer frontEndContainer = new FrontEndContainer();
        frontEndContainer.start();
        String frontEndUrl = frontEndContainer.getUrl();

        MongoConfiguration mongoConfiguration = new MongoConfiguration(mongoConnectionUrl, "tic-tac-toe", "dev");
        HttpConfiguration httpConfiguration = new HttpConfiguration(8080, List.of(frontEndUrl));
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(mongoConfiguration, httpConfiguration);

        logger.info("Front end URL: {}?API_URL={}", frontEndUrl, "http://localhost:%s".formatted(httpConfiguration.port()));

        ApiApp.run(applicationConfiguration);
    }
}
