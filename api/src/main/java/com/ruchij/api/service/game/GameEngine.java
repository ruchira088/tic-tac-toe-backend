package com.ruchij.api.service.game;

import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.exception.ValidationException;

import java.util.Optional;

public interface GameEngine {
    void checkMove(Game game, String playerId, Game.Coordinate coordinate) throws ValidationException;

    Optional<Game.Winner> getWinner(Game game);
}
