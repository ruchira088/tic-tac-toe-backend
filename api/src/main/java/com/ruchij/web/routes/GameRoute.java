package com.ruchij.web.routes;

import com.ruchij.dao.game.models.Game;
import com.ruchij.dao.game.models.PendingGame;
import com.ruchij.dao.user.models.User;
import com.ruchij.service.auth.AuthenticationService;
import com.ruchij.service.game.GameService;
import com.ruchij.web.middleware.Authenticator;
import com.ruchij.web.requests.NewGameRequest;
import com.ruchij.web.responses.PaginatedResponse;
import com.ruchij.web.responses.WebSocketResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.ws;

public class GameRoute implements EndpointGroup {
    private final GameService gameService;
    private final Authenticator authenticator;

    public GameRoute(GameService gameService, AuthenticationService authenticationService) {
        this.gameService = gameService;
        this.authenticator = new Authenticator(authenticationService);
    }

    @Override
    public void addEndpoints() {
        path("/pending", () -> {
            this.authenticator.post((user, context) -> {
                NewGameRequest newGameRequest = context.bodyAsClass(NewGameRequest.class);

                PendingGame pendingGame = this.gameService.createGame(newGameRequest.gameTitle(), user.id());

                context.status(HttpStatus.CREATED).json(pendingGame);
            });

            this.authenticator.get((user, context) -> {
                int offset = context.queryParamAsClass("offset", Integer.class).getOrDefault(0);
                int limit = context.queryParamAsClass("limit", Integer.class).getOrDefault(10);

                List<PendingGame> pendingGames = this.gameService.getPendingGames(limit, offset);

                context.status(HttpStatus.OK).json(new PaginatedResponse<>(pendingGames, offset, limit));
            });

            path("/id/{gameId}", () -> {
                this.authenticator.get((user, context) -> {
                    String gameId = context.pathParam("gameId");

                    PendingGame pendingGame = this.gameService.getPendingGameById(gameId);

                    context.status(HttpStatus.OK).json(pendingGame);
                });

                this.authenticator.post("/join", (user, context) -> {
                    String gameId = context.pathParam("gameId");

                    Game game = this.gameService.startGame(gameId, user.id());

                    context.status(HttpStatus.OK).json(game);
                });
            });
        });

        path("/id/{gameId}", () -> {
            this.authenticator.get((user, context) -> {
                String gameId = context.pathParam("gameId");

                Game game = this.gameService.getGameById(gameId);

                context.status(HttpStatus.OK).json(game);
            });

            this.authenticator.post("/move", (user, context) -> {
                String gameId = context.pathParam("gameId");
                Game.Coordinate coordinate = context.bodyAsClass(Game.Coordinate.class);

                Game game = this.gameService.addMove(gameId, user.id(), coordinate);
                context.status(HttpStatus.OK).json(game);
            });

            ws("/updates", ws -> {
                ws.onConnect(wsConnectContext -> {
                    User user = this.authenticator.authenticate(wsConnectContext);

                    wsConnectContext.enableAutomaticPings();
                    String gameId = wsConnectContext.pathParam("gameId");

                    String registrationId =
                        this.gameService.registerForUpdates(
                            gameId,
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
