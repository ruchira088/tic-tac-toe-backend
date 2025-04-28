package com.ruchij.web.routes;

import com.ruchij.dao.game.models.Game;
import com.ruchij.dao.game.models.PendingGame;
import com.ruchij.dao.user.models.User;
import com.ruchij.service.game.GameService;
import com.ruchij.utils.Either;
import com.ruchij.web.middleware.Authenticator;
import com.ruchij.web.requests.NewGameRequest;
import com.ruchij.web.responses.WebSocketResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GameRoute implements EndpointGroup {
    private final GameService gameService;
    private final Authenticator authenticator;

    public GameRoute(GameService gameService, Authenticator authenticator) {
        this.gameService = gameService;
        this.authenticator = authenticator;
    }

    @Override
    public void addEndpoints() {
        post("/", context -> {
            User user = this.authenticator.authenticate(context);
            NewGameRequest newGameRequest = context.bodyAsClass(NewGameRequest.class);

            PendingGame pendingGame = this.gameService.createGame(newGameRequest.gameTitle(), user.id());

            context.status(HttpStatus.CREATED).json(pendingGame);
        });

        path("/id/{gameId}", () -> {
            get(context -> {
                String gameId = context.pathParam("gameId");
                Either<PendingGame, Game> eitherGame = this.gameService.findGameById(gameId);

                Record result = eitherGame.fold(pendingGame -> pendingGame, game -> game);

                context.status(HttpStatus.OK).json(result);
            });

            post("/join", context -> {
                User user = this.authenticator.authenticate(context);
                String gameId = context.pathParam("gameId");

                Game game = this.gameService.startGame(gameId, user.id());

                context.status(HttpStatus.OK).json(game);
            });

            post("/move", context -> {
                User user = this.authenticator.authenticate(context);
                String gameId = context.pathParam("gameId");
                Game.Coordinate coordinate = context.bodyAsClass(Game.Coordinate.class);

                Game game = this.gameService.addMove(gameId, user.id(), coordinate);
                context.status(HttpStatus.OK).json(game);
            });

            ws("/updates", ws -> {
                ws.onConnect(wsConnectContext -> {
                    wsConnectContext.enableAutomaticPings();
                    User user = this.authenticator.authenticate(wsConnectContext.headerMap());
                    String gameId = wsConnectContext.pathParam("gameId");

                    String registrationId =
                        this.gameService.registerForUpdates(
                            gameId,
                            user.id(),
                            move -> {
                                wsConnectContext.send(new WebSocketResponse<>(WebSocketResponse.Type.MOVE_UPDATE, move));
                            },
                            winner -> {
                                wsConnectContext.send(new WebSocketResponse<>(WebSocketResponse.Type.WINNER, winner));
                            }
                        );

                    ws.onClose(wsCloseContext -> {
                        this.gameService.unregisterForUpdates(registrationId);
                    });
                });
            });
        });
    }
}
