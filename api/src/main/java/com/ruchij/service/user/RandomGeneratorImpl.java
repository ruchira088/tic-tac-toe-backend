package com.ruchij.service.user;

import com.github.javafaker.Faker;
import com.ruchij.dao.user.UserDao;
import com.ruchij.dao.user.models.User;

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

        Optional<User> user = userDao.findByName(username);

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
}
