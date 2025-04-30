package com.ruchij.dao.auth;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.ruchij.dao.auth.models.AuthToken;
import org.bson.Document;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MongoAuthTokenDaoImpl implements AuthTokenDao {
    private final MongoCollection<AuthToken> authTokenCollection;

    public MongoAuthTokenDaoImpl(MongoDatabase mongoDatabase, String collectionNameSuffix) {
        this.authTokenCollection = mongoDatabase.getCollection("auth_tokens-%s".formatted(collectionNameSuffix), AuthToken.class);
        this.authTokenCollection.createIndex(
            Indexes.ascending("issuedAt"), new IndexOptions().expireAfter(30L, TimeUnit.DAYS)
        );
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
