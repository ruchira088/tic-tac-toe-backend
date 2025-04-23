package com.ruchij.web.routes;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.post;

public class GameRoute implements EndpointGroup {
    @Override
    public void addEndpoints() {
        post("/", context -> {

        });
    }
}
