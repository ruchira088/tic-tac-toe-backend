package com.ruchij.config;

import com.typesafe.config.Config;

public record MongoConfiguration(String connectionUrl, String database) {
    public static MongoConfiguration parse(Config config) {
        String connectionUrl = config.getString("connection-url");
        String database = config.getString("database");

        MongoConfiguration mongoConfiguration = new MongoConfiguration(connectionUrl, database);

        return mongoConfiguration;
    }
}
