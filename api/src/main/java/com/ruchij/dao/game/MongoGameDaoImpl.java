package com.ruchij.dao.game;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.ruchij.dao.game.models.Game;
import com.ruchij.dao.game.models.PendingGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoGameDaoImpl implements GameDao {
    private final MongoCollection<PendingGame> pendingGamesCollection;
    private final MongoCollection<Game> gamesCollection;

    public MongoGameDaoImpl(MongoDatabase mongoDatabase, String collectionNameSuffix) {
        this.pendingGamesCollection = mongoDatabase.getCollection("pending_games-%s".formatted(collectionNameSuffix), PendingGame.class);
        this.gamesCollection = mongoDatabase.getCollection("games", Game.class);
    }

    @Override
    public PendingGame insertPendingGame(PendingGame pendingGame) {
        InsertOneResult insertOneResult = this.pendingGamesCollection.insertOne(pendingGame);

        return pendingGame;
    }

    @Override
    public Optional<PendingGame> updatePendingGame(PendingGame pendingGame) {
        UpdateResult updateResult =
            this.pendingGamesCollection.replaceOne(Filters.eq("_id", pendingGame.id()), pendingGame);

        if (updateResult.getModifiedCount() > 0) {
            return Optional.of(pendingGame);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<PendingGame> findPendingGameById(String pendingGameId) {
        Optional<PendingGame> pendingGame = Optional.ofNullable(
            this.pendingGamesCollection
                .find(Filters.eq("_id", pendingGameId))
                .first()
        );

        return pendingGame;
    }

    @Override
    public Game insertGame(Game game) {
        InsertOneResult insertOneResult = this.gamesCollection.insertOne(game);

        return game;
    }

    @Override
    public Optional<Game> findGameById(String gameId) {
        return Optional.ofNullable(this.gamesCollection.find(Filters.eq("_id", gameId)).first());
    }

    @Override
    public Optional<Game> updateGame(Game game) {
        UpdateResult updateResult = this.gamesCollection.replaceOne(Filters.eq("_id", game.id()), game);

        if (updateResult.getModifiedCount() > 0) {
            return Optional.of(game);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<PendingGame> getPendingGames(int limit, int offset) {
        return this.pendingGamesCollection.find().skip(offset).limit(limit).into(new ArrayList<>());
    }
}
