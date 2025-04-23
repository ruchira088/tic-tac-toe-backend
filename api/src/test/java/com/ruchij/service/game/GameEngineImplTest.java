package com.ruchij.service.game;

import com.ruchij.dao.game.models.Game;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineImplTest {
    private static final GameEngineImpl gameEngineImpl = new GameEngineImpl();

    @Test
    void isHorizontalWinner() {
        /*
            X X X
            * * *
            * * *
         */
        Assertions.assertTrue(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertTrue(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertTrue(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertTrue(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertTrue(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertTrue(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertTrue(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertTrue(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertFalse(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertFalse(gameEngineImpl.isWinner(
                List.of(
                        new Game.Coordinate(1, 1),
                        new Game.Coordinate(1, 1),
                        new Game.Coordinate(0, 0)
                )
        ));

        /*
             * X *
             * X *
             * * X
         */
        Assertions.assertFalse(gameEngineImpl.isWinner(
                List.of(
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
        Assertions.assertFalse(gameEngineImpl.isWinner(
                List.of(
                        new Game.Coordinate(2, 0),
                        new Game.Coordinate(1, 1),
                        new Game.Coordinate(2, 2)
                )
        ));
    }
}