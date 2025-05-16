package com.ruchij.api.service.game;

import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.exception.ValidationException;

import java.util.*;
import java.util.stream.Collectors;

public class GameEngineImpl implements GameEngine {
    private static final int DEFAULT_GRID_SIZE = 3;
    private final int gridSize;

    public GameEngineImpl(int gridSize) {
        if (gridSize < 1) {
            throw new IllegalArgumentException("gridSize must be greater than 0");
        }

        this.gridSize = gridSize;
    }

    public GameEngineImpl() {
        this(DEFAULT_GRID_SIZE);
    }

    @Override
    public void checkMove(Game game, String playerId, Game.Coordinate coordinate) throws ValidationException {
        if (game.winner().isPresent()) {
            throw new ValidationException("Game gameId=%s already has a winner".formatted(game.id()));
        }

        if (!Set.of(game.playerOneId(), game.playerTwoId()).contains(playerId)) {
            throw new ValidationException("playerId=%s is not a player in gameId=%s".formatted(playerId, game.id()));
        }

        boolean isVacant = game.moves().stream()
            .skip(Math.max(0, game.moves().size() - gridSize * 2))
            .noneMatch(move -> move.coordinate().equals(coordinate));

        if (!isVacant) {
            throw new ValidationException("%s is NOT vacant".formatted(coordinate));
        }

        boolean isPlayerTurn =
            (game.moves().isEmpty() && game.playerOneId().equals(playerId)) ||
                !game.moves().getLast().playerId().equals(playerId);

        if (!isPlayerTurn) {
            throw new ValidationException(
                "It is NOT the current turn for playerId=%s in gameId=%s"
                    .formatted(playerId, game.moves())
            );
        }

        if (coordinate.x() < 0 || coordinate.x() >= gridSize || coordinate.y() < 0 || coordinate.y() >= gridSize) {
            throw new ValidationException("Coordinate x=%s, y=%s is out of bounds".formatted(coordinate.x(), coordinate.y()));
        }
    }

    @Override
    public Optional<Game.Winner> getWinner(Game game) {
        Map<String, List<Game.Move>> movesByPlayer = game.moves().stream()
            .skip(Math.max(0, game.moves().size() - gridSize * 2))
            .collect(Collectors.groupingBy(move -> move.playerId()));

        Optional<Game.Winner> winner = movesByPlayer.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= gridSize)
            .flatMap(entry -> {
                Set<Game.Coordinate> coordinates =
                    entry.getValue().stream().map(Game.Move::coordinate).collect(Collectors.toSet());

                return isWinner(coordinates)
                    .map(winningCondition ->
                        new Game.Winner(entry.getKey(), winningCondition.winningRule, winningCondition.coordinates)
                    )
                    .stream();
            })
            .findFirst();

        return winner;
    }

    Optional<WinningCondition> isWinner(Set<Game.Coordinate> coordinates) {
        if (coordinates.size() >= gridSize) {
            for (Game.Coordinate baseCoordinate : coordinates) {
                List<Game.Coordinate> sameHorizontalLineCoordinates = coordinates.stream()
                    .filter(coordinate -> coordinate.y() == baseCoordinate.y())
                    .sorted(Comparator.comparing(Game.Coordinate::x))
                    .toList();

                boolean isHorizontalWin = sameHorizontalLineCoordinates.size() == gridSize;

                if (isHorizontalWin) {
                    return Optional.of(WinningCondition.of(Game.WinningRule.Horizontal, sameHorizontalLineCoordinates));
                }

                List<Game.Coordinate> sameVerticalLineCoordinates =
                    coordinates.stream()
                        .filter(coordinate -> coordinate.x() == baseCoordinate.x())
                        .sorted(Comparator.comparing(Game.Coordinate::y))
                        .toList();

                boolean isVerticalWin = sameVerticalLineCoordinates.size() == gridSize;

                if (isVerticalWin) {
                    return Optional.of(WinningCondition.of(Game.WinningRule.Vertical, sameVerticalLineCoordinates));
                }

                List<Game.Coordinate> backDiagonal = new ArrayList<>(gridSize);
                List<Game.Coordinate> forwardDiagonal = new ArrayList<>(gridSize);

                for (int i = 0; i < gridSize; i++) {
                    backDiagonal.add(new Game.Coordinate(i, i));
                    forwardDiagonal.add(new Game.Coordinate(gridSize - 1 - i, i));
                }

                boolean isRightDiagonalWin = coordinates.containsAll(backDiagonal);

                if (isRightDiagonalWin) {
                    return Optional.of(WinningCondition.of(Game.WinningRule.BackwardDiagonal, backDiagonal));
                }

                boolean isLeftDiagonalWin = coordinates.containsAll(forwardDiagonal);

                if (isLeftDiagonalWin) {
                    return Optional.of(WinningCondition.of(Game.WinningRule.ForwardDiagonal, forwardDiagonal));
                }
            }
        }

        return Optional.empty();
    }

    record WinningCondition(Game.WinningRule winningRule, List<Game.Coordinate> coordinates) {
        public static WinningCondition of(Game.WinningRule winningRule, List<Game.Coordinate> coordinates) {
            return new WinningCondition(winningRule, coordinates);
        }
    }

}
