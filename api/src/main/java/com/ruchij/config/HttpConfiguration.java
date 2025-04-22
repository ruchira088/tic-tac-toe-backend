package com.ruchij.config;

import com.typesafe.config.Config;

public record HttpConfiguration(int port) {
    public static HttpConfiguration parse(Config config) {
        return new HttpConfiguration(config.getInt("port"));
    }
}