package com.ruchij.api.service.game;

import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.dao.game.models.PendingGame;
import com.ruchij.api.exception.ResourceConflictException;
import com.ruchij.api.exception.ResourceNotFoundException;
import com.ruchij.api.exception.ValidationException;

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

    Game getGameById(String gameId) throws ResourceNotFoundException;

    PendingGame getPendingGameById(String pendingGameId) throws ResourceNotFoundException;

    String registerForUpdates(
        String gameId,
        Consumer<Game.Move> moveUpdates,
        Consumer<Game.Winner> winnerUpdates
    ) throws ResourceNotFoundException;

    void unregisterForUpdates(String registrationId);

    List<PendingGame> getPendingGames(int limit, int offset);
}
