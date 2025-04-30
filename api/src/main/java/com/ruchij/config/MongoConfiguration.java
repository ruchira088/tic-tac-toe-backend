package com.ruchij.config;

import com.typesafe.config.Config;

public record MongoConfiguration(String connectionUrl, String database, String collectionNameSuffix) {
    public static MongoConfiguration parse(Config config) {
        String connectionUrl = config.getString("connection-url");
        String database = config.getString("database");
        String collectionNameSuffix = config.getString("collection-name-suffix");

        MongoConfiguration mongoConfiguration = new MongoConfiguration(connectionUrl, database, collectionNameSuffix);

        return mongoConfiguration;
    }
}
