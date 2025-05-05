package com.ruchij.dev;

import com.ruchij.api.ApiApp;
import com.ruchij.api.config.ApplicationConfiguration;
import com.ruchij.api.config.HttpConfiguration;
import com.ruchij.api.config.MongoConfiguration;
import org.testcontainers.containers.MongoDBContainer;

import java.io.IOException;

public class DevApp {
    public static void main(String[] args) throws IOException {
        MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8");
        mongoDBContainer.start();
        String mongoConnectionUrl = mongoDBContainer.getConnectionString();

        MongoConfiguration mongoConfiguration = new MongoConfiguration(mongoConnectionUrl, "tic-tac-toe", "dev");
        HttpConfiguration httpConfiguration = new HttpConfiguration(8080);
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(mongoConfiguration, httpConfiguration);

        ApiApp.run(applicationConfiguration);
    }
}
