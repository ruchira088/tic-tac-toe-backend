package com.ruchij.api.dao.game;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.dao.game.models.PendingGame;
import org.bson.codecs.pojo.annotations.BsonId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoGameDaoImpl implements GameDao {
    private final MongoCollection<MongoPendingGame> pendingGamesCollection;
    private final MongoCollection<MongoGame> gamesCollection;

    public MongoGameDaoImpl(MongoDatabase mongoDatabase, String collectionNameSuffix) {
        this.pendingGamesCollection = mongoDatabase.getCollection("pending-games-%s".formatted(collectionNameSuffix), MongoPendingGame.class);
        this.gamesCollection = mongoDatabase.getCollection("games-%s".formatted(collectionNameSuffix), MongoGame.class);
    }

    @Override
    public PendingGame insertPendingGame(PendingGame pendingGame) {
        MongoPendingGame mongoPendingGame = MongoPendingGame.fromPendingGame(pendingGame);
        InsertOneResult insertOneResult = this.pendingGamesCollection.insertOne(mongoPendingGame);

        return pendingGame;
    }

    @Override
    public Optional<PendingGame> updatePendingGame(PendingGame pendingGame) {
        MongoPendingGame mongoPendingGame = MongoPendingGame.fromPendingGame(pendingGame);
        UpdateResult updateResult =
            this.pendingGamesCollection.replaceOne(Filters.eq("_id", pendingGame.id()), mongoPendingGame);


        if (updateResult.getModifiedCount() == 1) {
            return Optional.of(pendingGame);
        } else if (updateResult.getModifiedCount() == 0) {
            return Optional.empty();
        } else {
            throw new IllegalStateException(
                "More than one pending game was updated."
            );
        }
    }

    @Override
    public Optional<PendingGame> findPendingGameById(String pendingGameId) {
        return Optional.ofNullable(
            this.pendingGamesCollection
                .find(Filters.eq("_id", pendingGameId))
                .first()
        ).map(MongoPendingGame::toPendingGame);
    }

    @Override
    public Game insertGame(Game game) {
        InsertOneResult insertOneResult = this.gamesCollection.insertOne(MongoGame.fromGame(game));

        return game;
    }

    @Override
    public Optional<Game> findGameById(String gameId) {
        return Optional.ofNullable(this.gamesCollection.find(Filters.eq("_id", gameId)).first())
            .map(MongoGame::toGame);
    }

    @Override
    public Optional<Game> updateGame(Game game) {
        UpdateResult updateResult = this.gamesCollection.replaceOne(
            Filters.eq("_id", game.id()),
            MongoGame.fromGame(game)
        );

        if (updateResult.getModifiedCount() > 0) {
            return Optional.of(game);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<PendingGame> getPendingGames(int limit, int offset) {
        List<MongoPendingGame> mongoPendingGames =
            this.pendingGamesCollection
                .find(Filters.eq("gameStartedAt", null))
                .skip(offset)
                .limit(limit)
                .into(new ArrayList<>());

        return mongoPendingGames.stream().map(MongoPendingGame::toPendingGame).toList();
    }

    public record MongoGame(
        @BsonId String id,
        String title,
        Instant createdAt,
        String createdBy,
        Instant startedAt,
        String playerOneId,
        String playerTwoId,
        List<Game.Move> moves,
        Game.Winner winner
    ) {
        public static MongoGame fromGame(Game game) {
            return new MongoGame(
                game.id(),
                game.title(),
                game.createdAt(),
                game.createdBy(),
                game.startedAt(),
                game.playerOneId(),
                game.playerTwoId(),
                game.moves(),
                game.winner().orElse(null)
            );
        }

        public Game toGame() {
            return new Game(
                id,
                title,
                createdAt,
                createdBy,
                startedAt,
                playerOneId,
                playerTwoId,
                moves,
                Optional.ofNullable(winner)
            );
        }
    }

    public record MongoPendingGame(
        @BsonId String id,
        String title,
        Instant createdAt,
        String createdBy,
        Instant gameStartedAt
    ) {
        public static MongoPendingGame fromPendingGame(PendingGame pendingGame) {
            return new MongoPendingGame(
                pendingGame.id(),
                pendingGame.title(),
                pendingGame.createdAt(),
                pendingGame.createdBy(),
                pendingGame.gameStartedAt().orElse(null)
            );
        }

        public PendingGame toPendingGame() {
            return new PendingGame(
                id,
                title,
                createdAt,
                createdBy,
                Optional.ofNullable(gameStartedAt)
            );
        }
    }
}
