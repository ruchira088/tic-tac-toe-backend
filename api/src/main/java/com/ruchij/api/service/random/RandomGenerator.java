package com.ruchij.api.service.random;

import java.util.UUID;

public interface RandomGenerator {
    String username();

    UUID uuid();

    String password();

    boolean booleanValue();
}
