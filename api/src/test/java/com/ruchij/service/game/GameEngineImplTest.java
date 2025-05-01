package com.ruchij.service.game;

import com.ruchij.dao.game.models.Game;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

class GameEngineImplTest {
    private static final GameEngineImpl gameEngineImpl = new GameEngineImpl();

    @Test
    void isHorizontalWinner() {
        /*
            X X X
            * * *
            * * *
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Horizontal), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(1, 0),
                new Game.Coordinate(2, 0)
            )
        ));

        /*
            * * *
            X X X
            * * *
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Horizontal), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 1),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(2, 1)
            )
        ));

        /*
            * * *
            * * *
            X X X
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Horizontal), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 2),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2)
            )
        ));

        /*
            * * *
            X X *
            X X X
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Horizontal), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 1),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2)
            )
        ));
    }

    @Test
    void isVerticalWinner() {
        /*
            X * *
            X * *
            X * *
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Vertical), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 0),
                new Game.Coordinate(0, 1),
                new Game.Coordinate(0, 2)
            )
        ));

         /*
            * X *
            * X *
            * X *
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Vertical), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(1, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(1, 2)
            )
        ));

        /*
            * * X
            * * X
            * * X
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Vertical), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(2, 1),
                new Game.Coordinate(2, 2)
            )
        ));

        /*
         * * X
         * X X
         * X X
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Vertical), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(1, 1),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 0),
                new Game.Coordinate(2, 1),
                new Game.Coordinate(2, 2)
            )
        ));
    }

    @Test
    void isRightDiagonalWinner() {
        /*
             * * X
             * X *
             X * *
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Diagonal), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(2, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2)
            )
        ));

        /*
             * X X
             * X X
             X * *
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Diagonal), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(1, 0),
                new Game.Coordinate(2, 1),
                new Game.Coordinate(2, 0),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 2)
            )
        ));
    }

    @Test
    void isLeftDiagonalWinner() {
        /*
             X * *
             * X *
             * * X
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Diagonal), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(2, 2),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 0)
            )
        ));

        /*
             X * *
             X X *
             * X X
         */
        Assertions.assertEquals(Optional.of(Game.WinningRule.Diagonal), gameEngineImpl.isWinner(
            Set.of(
                new Game.Coordinate(0, 1),
                new Game.Coordinate(1, 2),
                new Game.Coordinate(2, 2),
                new Game.Coordinate(1, 1),
                new Game.Coordinate(0, 0)
            )
        ));
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
}