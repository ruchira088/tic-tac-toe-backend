package com.ruchij.service.game;

import com.ruchij.dao.game.models.Game;
import com.ruchij.exception.ValidationException;

import java.util.Optional;

public interface GameEngine {
    Game.Player isValidMove(Game game, String playerId, Game.Coordinate coordinate) throws ValidationException;

    Optional<Game.Player> getWinner(Game game);
}
