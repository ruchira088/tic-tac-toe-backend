package com.ruchij.dao.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.ruchij.dao.user.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class MongoUserDaoImpl implements UserDao {
    private final MongoCollection<User> userCollection;

    public MongoUserDaoImpl(MongoDatabase mongoDatabase) {
        this.userCollection = mongoDatabase.getCollection("users", User.class);
    }

    @Override
    public User insert(User user) {
        InsertOneResult insertOneResult = userCollection.insertOne(user);

        return user;
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(userCollection.find(Filters.eq("_id", userId)).first());
    }

    @Override
    public Optional<User> findByName(String name) {
        return Optional.ofNullable(userCollection.find(Filters.eq("name", name)).first());
    }

    @Override
    public List<User> searchByName(String name) {
        return userCollection
                .find(Filters.regex("name", Pattern.compile(name)))
                .into(new ArrayList<>());
    }
}
