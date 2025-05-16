package com.ruchij.api.service.game;

import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.exception.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

class GameEngineImplTest {
    private static final GameEngineImpl gameEngineImpl = new GameEngineImpl();
    private static final GameEngineImpl gameEngineImpl4x4 = new GameEngineImpl(4);

    private String moveId() {
        return UUID.randomUUID().toString();
    }

    @Test
    void isHorizontalWinner() {
        /*
            X X X
            * X *
            * * *
         */
        Optional<GameEngineImpl.WinningCondition> result = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(1, 0),
                new Game.Coordinate(2, 0)
            )
        );
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(Game.WinningRule.Horizontal, result.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(1, 0),
                new Game.Coordinate(2, 0)
            ),
            result.get().coordinates()
        );

        /*
            * X *
            X X X
            * * *
         */
        Optional<GameEngineImpl.WinningCondition> result2 = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 1),
                new Game.Coordinate(1, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(2, 1)
            )
        );
        Assertions.assertTrue(result2.isPresent());
        Assertions.assertEquals(Game.WinningRule.Horizontal, result2.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(0, 1),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(2, 1)
            ),
            result2.get().coordinates()
        );

        /*
            * * *
            * X *
            X X X
         */
        Optional<GameEngineImpl.WinningCondition> result3 = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 2),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2)
            )
        );
        Assertions.assertTrue(result3.isPresent());
        Assertions.assertEquals(Game.WinningRule.Horizontal, result3.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(0, 2),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2)
            ),
            result3.get().coordinates()
        );

        /*
            * * *
            X X *
            X X X
         */
        Optional<GameEngineImpl.WinningCondition> result4 = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 1),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2)
            )
        );
        Assertions.assertTrue(result4.isPresent());
        Assertions.assertEquals(Game.WinningRule.Horizontal, result4.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(0, 2),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2)
            ),
            result4.get().coordinates()
        );
    }

    @Test
    void isVerticalWinner() {
        /*
            X * *
            X X *
            X * *
         */
        Optional<GameEngineImpl.WinningCondition> result = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(0, 1),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2)
            )
        );
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(Game.WinningRule.Vertical, result.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(0, 1),
                new Game.Coordinate(0, 2)
            ),
            result.get().coordinates()
        );

        /*
             X X *
             * X *
             * X *
         */
        Optional<GameEngineImpl.WinningCondition> result2 = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(1, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(1, 2)
            )
        );
        Assertions.assertTrue(result2.isPresent());
        Assertions.assertEquals(Game.WinningRule.Vertical, result2.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(1, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(1, 2)
            ),
            result2.get().coordinates()
        );

        /*
         * * X
         * * X
         * X X
         */
        Optional<GameEngineImpl.WinningCondition> result3 = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(2, 1),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2)
            )
        );
        Assertions.assertTrue(result3.isPresent());
        Assertions.assertEquals(Game.WinningRule.Vertical, result3.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(2, 1),
                new Game.Coordinate(2, 2)
            ),
            result3.get().coordinates()
        );

        /*
         * * X
         * X X
         * X X
         */
        Optional<GameEngineImpl.WinningCondition> result4 = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(1, 1),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 0),
                new Game.Coordinate(2, 1),
                new Game.Coordinate(2, 2)
            )
        );
        Assertions.assertTrue(result4.isPresent());
        Assertions.assertEquals(Game.WinningRule.Vertical, result4.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(2, 1),
                new Game.Coordinate(2, 2)
            ),
            result4.get().coordinates()
        );
    }

    @Test
    void isRightDiagonalWinner() {
        /*
            * * X
            * X *
            X * *
         */
        Optional<GameEngineImpl.WinningCondition> result = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2)
            )
        );
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(Game.WinningRule.ForwardDiagonal, result.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2)
            ),
            result.get().coordinates()
        );

        /*
             * X X
             * X X
             X * *
         */
        Optional<GameEngineImpl.WinningCondition> result2 = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(1, 0),
                new Game.Coordinate(2, 1),
                new Game.Coordinate(2, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2)
            )
        );
        Assertions.assertTrue(result2.isPresent());
        Assertions.assertEquals(Game.WinningRule.ForwardDiagonal, result2.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2)
            ),
            result2.get().coordinates()
        );
    }

    @Test
    void isLeftDiagonalWinner() {
        /*
             X * *
             * X *
             * * X
         */
        Optional<GameEngineImpl.WinningCondition> result = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(2, 2),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 0)
            )
        );
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(Game.WinningRule.BackwardDiagonal, result.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(2, 2)
            ),
            result.get().coordinates()
        );

        /*
             X * *
             X X *
             * X X
         */
        Optional<GameEngineImpl.WinningCondition> result2 = gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 1),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 0)
            )
        );
        Assertions.assertTrue(result2.isPresent());
        Assertions.assertEquals(Game.WinningRule.BackwardDiagonal, result2.get().winningRule());
        Assertions.assertEquals(
            List.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(2, 2)
            ),
            result2.get().coordinates()
        );
    }

    @Test
    void shouldNotBeWinners() {
        /*
             X * *
             * X *
             * X *
         */
        Assertions.assertEquals(Optional.empty(), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(1, 2),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 0)
            )
        ));

        /*
             X * *
             * X *
             * * *
         */
        Assertions.assertEquals(Optional.empty(), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 0)
            )
        ));

        /*
         * X *
         * X *
         * * X
         */
        Assertions.assertEquals(Optional.empty(), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(1, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(2, 2)
            )
        ));

        /*
         * * X
         * X *
         * * X
         */
        Assertions.assertEquals(Optional.empty(), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(2, 2)
            )
        ));
    }

    @Test
    void testCheckMoveValid() {
        /*
         Board state:
            * * *
            * X *
            * * O

         Valid move for player1 at (0,0):
            X * *
            * X *
            * * O
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        // This should not throw an exception
        Assertions.assertDoesNotThrow(() ->
            gameEngineImpl.checkMove(game, player1Id, new Game.Coordinate(0, 0))
        );
    }

    @Test
    void testCheckMoveInvalidPlayerNotInGame() {
        /*
         Board state:
            * * *
            * X *
            * * O

         Invalid move: player3 is not in the game
        */
        String player1Id = "player1";
        String player2Id = "player2";
        String player3Id = "player3";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () ->
            gameEngineImpl.checkMove(game, player3Id, new Game.Coordinate(0, 0))
        );

        Assertions.assertTrue(exception.getMessage().contains("is not a player in gameId"));
    }

    @Test
    void testCheckMoveInvalidNotPlayerTurn() {
        /*
         Board state:
            * * *
            * X *
            * * O

         Invalid move: player2 just moved, so it's player1's turn
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () ->
            gameEngineImpl.checkMove(game, player2Id, new Game.Coordinate(0, 0))
        );

        Assertions.assertTrue(exception.getMessage().contains("It is NOT the current turn"));
    }

    @Test
    void testCheckMoveInvalidOccupiedSpace() {
        /*
         Board state:
            * * *
            * X *
            * * O

         Invalid move: trying to place at (1,1) which is already occupied
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () ->
            gameEngineImpl.checkMove(game, player1Id, new Game.Coordinate(1, 1))
        );

        Assertions.assertTrue(exception.getMessage().contains("is NOT vacant"));
    }

    @Test
    void testCheckMoveInvalidOutOfBounds() {
        /*
         Board state:
            * * *
            * X *
            * * O

         Invalid move: trying to place at (3,3) which is out of bounds for 3x3 grid
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () ->
            gameEngineImpl.checkMove(game, player1Id, new Game.Coordinate(3, 3))
        );

        Assertions.assertTrue(exception.getMessage().contains("out of bounds"));
    }

    @Test
    void testCheckMoveInvalidGameAlreadyWon() {
        /*
         Board state:
            X X X
            * O *
            * * O

         Game already has a winner (player1 with horizontal win)
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 2)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 0)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.of(new Game.Winner(player1Id, Game.WinningRule.Horizontal, List.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(1, 0),
                new Game.Coordinate(2, 0)
            )))
        );

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () ->
            gameEngineImpl.checkMove(game, player2Id, new Game.Coordinate(0, 2))
        );

        Assertions.assertTrue(exception.getMessage().contains("already has a winner"));
    }

    @Test
    void testGetWinnerHorizontal() {
        /*
         Board state:
            X X X
            O O *
            * * *

         Player1 wins with horizontal line in row 0
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 0)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl.getWinner(game);

        Assertions.assertTrue(winner.isPresent());
        Assertions.assertEquals(player1Id, winner.get().playerId());
        Assertions.assertEquals(Game.WinningRule.Horizontal, winner.get().winningRule());

        // Verify winning coordinates
        List<Game.Coordinate> expectedCoordinates = List.of(
            new Game.Coordinate(0, 0),
            new Game.Coordinate(1, 0),
            new Game.Coordinate(2, 0)
        );
        Assertions.assertEquals(expectedCoordinates.size(), winner.get().coordinates().size());
        Assertions.assertTrue(winner.get().coordinates().containsAll(expectedCoordinates));
    }

    @Test
    void testGetWinnerVertical() {
        /*
         Board state:
            X O *
            X O *
            X * *

         Player1 wins with vertical line in column 0
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl.getWinner(game);

        Assertions.assertTrue(winner.isPresent());
        Assertions.assertEquals(player1Id, winner.get().playerId());
        Assertions.assertEquals(Game.WinningRule.Vertical, winner.get().winningRule());

        // Verify winning coordinates
        List<Game.Coordinate> expectedCoordinates = List.of(
            new Game.Coordinate(0, 0),
            new Game.Coordinate(0, 1),
            new Game.Coordinate(0, 2)
        );
        Assertions.assertEquals(expectedCoordinates.size(), winner.get().coordinates().size());
        Assertions.assertTrue(winner.get().coordinates().containsAll(expectedCoordinates));
    }

    @Test
    void testGetWinnerDiagonal() {
        /*
         Board state:
            X O *
            O X *
            * * X

         Player1 wins with diagonal line from top-left to bottom-right
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl.getWinner(game);

        Assertions.assertTrue(winner.isPresent());
        Assertions.assertEquals(player1Id, winner.get().playerId());
        Assertions.assertEquals(Game.WinningRule.BackwardDiagonal, winner.get().winningRule());

        // Verify winning coordinates
        List<Game.Coordinate> expectedCoordinates = List.of(
            new Game.Coordinate(0, 0),
            new Game.Coordinate(1, 1),
            new Game.Coordinate(2, 2)
        );
        Assertions.assertEquals(expectedCoordinates.size(), winner.get().coordinates().size());
        Assertions.assertTrue(winner.get().coordinates().containsAll(expectedCoordinates));
    }

    @Test
    void testGetWinnerAntiDiagonal() {
        /*
         Board state:
            * O X
            O X *
            X * *

         Player1 wins with diagonal line from top-right to bottom-left
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl.getWinner(game);

        Assertions.assertTrue(winner.isPresent());
        Assertions.assertEquals(player1Id, winner.get().playerId());
        Assertions.assertEquals(Game.WinningRule.ForwardDiagonal, winner.get().winningRule());

        // Verify winning coordinates
        List<Game.Coordinate> expectedCoordinates = List.of(
            new Game.Coordinate(2, 0),
            new Game.Coordinate(1, 1),
            new Game.Coordinate(0, 2)
        );
        Assertions.assertEquals(expectedCoordinates.size(), winner.get().coordinates().size());
        Assertions.assertTrue(winner.get().coordinates().containsAll(expectedCoordinates));
    }

    @Test
    void testGetWinnerNoWinner() {
        /*
         Board state:
            X O X
            O X O
            O * *

         No winner yet
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl.getWinner(game);

        Assertions.assertTrue(winner.isEmpty());
    }

    @Test
    void testGetWinnerDraw() {
        /*
         Board state:
            X O X
            X X O
            O X O

         Game is a draw (all spaces filled, no winner)
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 2)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 2)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl.getWinner(game);

        Assertions.assertTrue(winner.isEmpty());
    }

    @Test
    void testCustomGridSize() {
        // Test that constructor with custom grid size works
        Assertions.assertDoesNotThrow(() -> new GameEngineImpl(5));

        // Test that constructor with invalid grid size throws exception
        Assertions.assertThrows(IllegalArgumentException.class, () -> new GameEngineImpl(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new GameEngineImpl(-1));
    }

    @Test
    void testHorizontalWin4x4Grid() {
        /*
         Board state (4x4):
            X X X X
            O O * *
            * * * *
            * * * *

         Player1 wins with horizontal line in row 0
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(3, 0)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl4x4.getWinner(game);

        Assertions.assertTrue(winner.isPresent());
        Assertions.assertEquals(player1Id, winner.get().playerId());
        Assertions.assertEquals(Game.WinningRule.Horizontal, winner.get().winningRule());
    }

    @Test
    void testVerticalWin4x4Grid() {
        /*
         Board state (4x4):
            X O * *
            X O * *
            X * * *
            X * * *

         Player1 wins with vertical line in column 0
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 2)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 3)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl4x4.getWinner(game);

        Assertions.assertTrue(winner.isPresent());
        Assertions.assertEquals(player1Id, winner.get().playerId());
        Assertions.assertEquals(Game.WinningRule.Vertical, winner.get().winningRule());
    }

    @Test
    void testDiagonalWin4x4Grid() {
        /*
         Board state (4x4):
            X O * *
            O X * *
            * * X *
            * * * X

         Player1 wins with diagonal line from top-left to bottom-right
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 1)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 2)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(3, 3)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl4x4.getWinner(game);

        Assertions.assertTrue(winner.isPresent());
        Assertions.assertEquals(player1Id, winner.get().playerId());
        Assertions.assertEquals(Game.WinningRule.BackwardDiagonal, winner.get().winningRule());
    }

    @Test
    void testAntiDiagonalWin4x4Grid() {
        /*
         Board state (4x4):
            * * * X
            * * X *
            * X * *
            X * * *

         Player1 wins with diagonal line from top-right to bottom-left
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(3, 0)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(0, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(2, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(1, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 2)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 0)));
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(0, 3)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        Optional<Game.Winner> winner = gameEngineImpl4x4.getWinner(game);

        Assertions.assertTrue(winner.isPresent());
        Assertions.assertEquals(player1Id, winner.get().playerId());
        Assertions.assertEquals(Game.WinningRule.ForwardDiagonal, winner.get().winningRule());
    }

    @Test
    void testCheckMoveOutOfBounds4x4Grid() {
        /*
         Board state (4x4):
            * * * *
            * X * *
            * * O *
            * * * *

         Invalid move: trying to place at (4,4) which is out of bounds for 4x4 grid
        */
        String player1Id = "player1";
        String player2Id = "player2";

        List<Game.Move> moves = new ArrayList<>();
        moves.add(new Game.Move(moveId(), player1Id, Instant.now(), new Game.Coordinate(1, 1)));
        moves.add(new Game.Move(moveId(), player2Id, Instant.now(), new Game.Coordinate(2, 2)));

        Game game = new Game(
            "game-id",
            "Test Game",
            Instant.now(),
            player1Id,
            Instant.now(),
            player1Id,
            player2Id,
            moves,
            Optional.empty()
        );

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () ->
            gameEngineImpl4x4.checkMove(game, player1Id, new Game.Coordinate(4, 4))
        );

        Assertions.assertTrue(exception.getMessage().contains("out of bounds"));
    }
}
