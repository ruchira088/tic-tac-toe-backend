package com.ruchij.service.game;

import com.ruchij.dao.game.models.Game;
import com.ruchij.dao.game.models.PendingGame;
import com.ruchij.exception.ResourceConflictException;
import com.ruchij.exception.ResourceNotFoundException;
import com.ruchij.exception.ValidationException;

import java.util.List;
import java.util.function.Consumer;

public interface GameService {
    PendingGame createGame(String name, String playerId);

    Game startGame(String pendingGameId, String otherPlayerId)
        throws ResourceNotFoundException, ResourceConflictException;

    Game addMove(
        String gameId,
        String playerId,
        Game.Coordinate coordinate
    ) throws ResourceNotFoundException, ValidationException;

    Game findGameById(String gameId) throws ResourceNotFoundException;

    PendingGame findPendingGameById(String pendingGameId) throws ResourceNotFoundException;

    String registerForUpdates(
        String gameId,
        Consumer<Game.Move> moveUpdates,
        Consumer<Game.Winner> winnerUpdates
    ) throws ResourceNotFoundException;

    void unregisterForUpdates(String registrationId);

    List<PendingGame> getPendingGames(int limit, int offset);
}
