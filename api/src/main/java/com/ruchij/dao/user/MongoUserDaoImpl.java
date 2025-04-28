package com.ruchij.dao.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.ruchij.dao.user.models.User;
import com.ruchij.dao.user.models.UserCredentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class MongoUserDaoImpl implements UserDao {
    private final MongoCollection<User> userCollection;
    private final MongoCollection<UserCredentials> userCredentialsCollection;

    public MongoUserDaoImpl(MongoDatabase mongoDatabase) {
        this.userCollection = mongoDatabase.getCollection("users", User.class);
        this.userCredentialsCollection = mongoDatabase.getCollection("user_credentials", UserCredentials.class);
    }

    @Override
    public User insert(User user) {
        InsertOneResult insertOneResult = this.userCollection.insertOne(user);

        return user;
    }

    @Override
    public UserCredentials insert(UserCredentials userCredentials) {
        InsertOneResult insertOneResult = this.userCredentialsCollection.insertOne(userCredentials);

        return userCredentials;
    }

    @Override
    public Optional<UserCredentials> findCredentialsById(String userId) {
        return Optional.ofNullable(this.userCredentialsCollection.find(Filters.eq("_id", userId)).first());
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(this.userCollection.find(Filters.eq("_id", userId)).first());
    }

    @Override
    public Optional<User> findByName(String name) {
        return Optional.ofNullable(this.userCollection.find(Filters.eq("name", name)).first());
    }

    @Override
    public List<User> searchByName(String name) {
        return this.userCollection
                .find(Filters.regex("name", Pattern.compile(name)))
                .into(new ArrayList<>());
    }
}
