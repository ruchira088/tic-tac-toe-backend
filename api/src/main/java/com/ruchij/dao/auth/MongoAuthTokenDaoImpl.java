package com.ruchij.dao.auth;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.ruchij.dao.auth.models.AuthToken;

import java.util.Optional;

public class MongoAuthTokenDaoImpl implements AuthTokenDao {
    private final MongoCollection<AuthToken> authTokenCollection;

    public MongoAuthTokenDaoImpl(MongoDatabase mongoDatabase) {
        this.authTokenCollection = mongoDatabase.getCollection("auth_tokens", AuthToken.class);
    }

    @Override
    public AuthToken insert(AuthToken authToken) {
        InsertOneResult insertOneResult = this.authTokenCollection.insertOne(authToken);

        return authToken;
    }

    @Override
    public Optional<AuthToken> findByToken(String token) {
        return Optional.ofNullable(this.authTokenCollection.find(Filters.eq("_id", token)).first());
    }

    @Override
    public Optional<AuthToken> deleteByToken(String token) {
        return this.findByToken(token).map(authToken -> {
            DeleteResult deleteResult = this.authTokenCollection.deleteOne(Filters.eq("_id", token));
            return authToken;
        });
    }
}
