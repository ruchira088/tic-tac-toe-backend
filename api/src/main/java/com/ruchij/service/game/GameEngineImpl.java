package com.ruchij.service.game;

import com.ruchij.dao.game.models.Game;
import com.ruchij.exception.ValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameEngineImpl implements GameEngine {
    private static final int GRID_SIZE = 3;

    @Override
    public Game.Player isValidMove(Game game, String playerId, Game.Coordinate coordinate) throws ValidationException {
        Game.Player player;

        if (game.playerOneId().equalsIgnoreCase(playerId)) {
            player = Game.Player.PlayerOne;
        } else if (game.playerTwoId().equalsIgnoreCase(playerId)) {
            player = Game.Player.PlayerTwo;
        } else {
            throw new ValidationException("playerId=%s is not a player in gameId=%s".formatted(playerId, game.id()));
        }

        boolean isVacant = game.moves().stream()
                .skip(Math.max(0, game.moves().size() - GRID_SIZE * 2))
                .noneMatch(move -> move.coordinate().equals(coordinate));

        if (!isVacant) {
            throw new ValidationException("%s is NOT vacant".formatted(coordinate));
        }

        Game.Player currentTurn;

        if (game.moves().isEmpty()) {
            currentTurn = Game.Player.PlayerOne;
        } else {
            Game.Move lastMove = game.moves().getLast();

            if (lastMove.player() == Game.Player.PlayerOne) {
                currentTurn = Game.Player.PlayerTwo;
            } else {
                currentTurn = Game.Player.PlayerOne;
            }
        }

        if (currentTurn != player) {
            throw new ValidationException(
                    "It is NOT the current turn for playerId=%s in gameId=%s"
                            .formatted(playerId, game.moves())
            );
        }

        if (coordinate.x() < 0 || coordinate.x() >= GRID_SIZE || coordinate.y() < 0 || coordinate.y() >= GRID_SIZE) {
            throw new ValidationException("Coordinate x=%s, y=%s is out of bounds".formatted(coordinate.x(), coordinate.y()));
        }

        return player;
    }

    @Override
    public Optional<Game.Player> getWinner(Game game) {
        if (game.moves().size() < GRID_SIZE * 2 - 1) {
            return Optional.empty();
        }

        Map<Game.Player, List<Game.Move>> movesByPlayer = game.moves().stream()
                .skip(game.moves().size() - GRID_SIZE * 2)
                .collect(Collectors.groupingBy(move -> move.player()));

        return Optional.empty();
    }

    boolean isWinner(List<Game.Coordinate> coordinates) {
        if (coordinates.size() < GRID_SIZE) {
            return false;
        } else {
            HashSet<Game.Coordinate> coordinateHashSet = new HashSet<>(coordinates);

            if (coordinateHashSet.size() != coordinates.size()) {
                return false;
            }

            Game.Coordinate baseCoordinate = coordinates.getFirst();

            boolean isHorizontalWin =
                    coordinates.stream().allMatch(coordinate -> coordinate.y() == baseCoordinate.y());

            boolean isVerticalWin =
                    coordinates.stream().allMatch(coordinate -> coordinate.x() == baseCoordinate.x());

            HashSet<Game.Coordinate> rightDiagonal = new HashSet<>();
            HashSet<Game.Coordinate> leftDiagonal = new HashSet<>();

            for (int i = 0; i < GRID_SIZE; i++) {
                leftDiagonal.add(new Game.Coordinate(i, i));
                rightDiagonal.add(new Game.Coordinate(GRID_SIZE - 1 - i, i));
            }

            boolean isRightDiagonalWin = rightDiagonal.containsAll(coordinates);
            boolean isLeftDiagonalWin = leftDiagonal.containsAll(coordinates);

            return isHorizontalWin || isVerticalWin || isLeftDiagonalWin || isRightDiagonalWin;
        }
    }

}
