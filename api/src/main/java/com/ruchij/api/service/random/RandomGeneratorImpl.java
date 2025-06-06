package com.ruchij.api.service.random;

import com.github.javafaker.Faker;
import com.ruchij.api.dao.user.UserDao;
import com.ruchij.api.dao.user.models.User;

import java.util.Optional;
import java.util.UUID;

public class RandomGeneratorImpl implements RandomGenerator {
    private final UserDao userDao;
    private final Faker faker;

    public RandomGeneratorImpl(UserDao userDao, Faker faker) {
        this.userDao = userDao;
        this.faker = faker;
    }

    @Override
    public String username() {
        String color = this.faker.color().name();
        String animal = this.faker.animal().name();

        String username = "%s.%s".formatted(color, animal).replaceAll(" ", ".");

        Optional<User> user = userDao.findByUsername(username);

        if (user.isPresent()) {
            return username();
        } else {
            return username;
        }
    }

    @Override
    public UUID uuid() {
        return UUID.randomUUID();
    }

    @Override
    public String password() {
        return this.faker.internet().password(10, 20, true, true, true);
    }

    @Override
    public boolean booleanValue() {
        return this.faker.random().nextBoolean();
    }
}
