package com.ruchij.service.game;

import com.ruchij.dao.game.models.Game;
import com.ruchij.exception.ValidationException;

import java.util.Optional;

public interface GameEngine {
    void checkMove(Game game, String playerId, Game.Coordinate coordinate) throws ValidationException;

    Optional<Game.Winner> getWinner(Game game);
}
