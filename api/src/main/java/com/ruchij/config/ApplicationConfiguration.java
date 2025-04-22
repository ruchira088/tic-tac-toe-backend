package com.ruchij.config;

import com.typesafe.config.Config;

public record ApplicationConfiguration(HttpConfiguration httpConfiguration) {
    public static ApplicationConfiguration parse(Config config) {
        return new ApplicationConfiguration(
            HttpConfiguration.parse(config.getConfig("http"))
        );
    }
}
