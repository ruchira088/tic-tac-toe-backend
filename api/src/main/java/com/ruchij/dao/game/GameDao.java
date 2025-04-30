package com.ruchij.dao.game;

import com.ruchij.dao.game.models.Game;
import com.ruchij.dao.game.models.PendingGame;

import java.util.List;
import java.util.Optional;

public interface GameDao {
    PendingGame insertPendingGame(PendingGame pendingGame);

    Optional<PendingGame> updatePendingGame(PendingGame pendingGame);

    Optional<PendingGame> findPendingGameById(String pendingGameId);

    Game insertGame(Game game);

    Optional<Game> findGameById(String gameId);

    Optional<Game> updateGame(Game game);

    List<PendingGame> getPendingGames(int limit, int offset);
}
