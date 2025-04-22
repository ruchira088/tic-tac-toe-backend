package com.ruchij.config;

import com.typesafe.config.Config;

public record ApplicationConfiguration(MongoConfiguration mongoConfiguration, HttpConfiguration httpConfiguration) {
    public static ApplicationConfiguration parse(Config config) {
        return new ApplicationConfiguration(
                MongoConfiguration.parse(config.getConfig("mongo")),
                HttpConfiguration.parse(config.getConfig("http"))
        );
    }
}
