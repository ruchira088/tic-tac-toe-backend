package com.ruchij.service.game;

import com.ruchij.dao.game.models.Game;
import com.ruchij.dao.game.models.PendingGame;
import com.ruchij.exception.ResourceConflictException;
import com.ruchij.exception.ResourceNotFoundException;
import com.ruchij.exception.ValidationException;

public interface GameService {
    PendingGame createGame(String name, String playerId);

    Game startGame(String pendingGameId, String otherPlayerId)
            throws ResourceNotFoundException, ResourceConflictException;

    Game addMove(
            String gameId,
            String playerId,
            Game.Coordinate coordinate
            ) throws ResourceNotFoundException, ValidationException;
}
