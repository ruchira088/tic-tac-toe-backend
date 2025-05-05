package com.ruchij.api.dao.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.dao.user.models.UserCredentials;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class MongoUserDaoImpl implements UserDao {
    private final MongoCollection<MongoUser> userCollection;
    private final MongoCollection<UserCredentials> userCredentialsCollection;

    public MongoUserDaoImpl(MongoDatabase mongoDatabase, String collectionNameSuffix) {
        this.userCollection = mongoDatabase.getCollection("users-%s".formatted(collectionNameSuffix), MongoUser.class);
        this.userCredentialsCollection =
            mongoDatabase.getCollection("user-credentials-%s".formatted(collectionNameSuffix), UserCredentials.class);
    }

    @Override
    public User insert(User user) {
        InsertOneResult insertOneResult = this.userCollection.insertOne(MongoUser.fromUser(user));

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
        return Optional.ofNullable(this.userCollection.find(Filters.eq("_id", userId)).first())
            .map(MongoUser::toUser);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(this.userCollection.find(Filters.eq("username", username)).first())
            .map(MongoUser::toUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(this.userCollection.find(Filters.eq("email", email)).first())
            .map(MongoUser::toUser);
    }

    @Override
    public List<User> searchByUsername(String username) {
        return this.userCollection
            .find(Filters.regex("username", Pattern.compile(username)))
            .map(MongoUser::toUser)
            .into(new ArrayList<>());
    }

    public record MongoUser(@BsonId String id, String username, String email, Instant createdAt) {
        private static MongoUser fromUser(User user) {
            return new MongoUser(user.id(), user.username(), user.email().orElse(null), user.createdAt());
        }

        public User toUser() {
            return new User(this.id, this.username, Optional.ofNullable(this.email), this.createdAt);
        }
    }
}
