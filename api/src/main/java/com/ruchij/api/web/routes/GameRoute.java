package com.ruchij.api.web.routes;

import com.ruchij.api.dao.game.models.Game;
import com.ruchij.api.dao.game.models.PendingGame;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exception.ResourceNotFoundException;
import com.ruchij.api.service.auth.AuthenticationService;
import com.ruchij.api.service.game.GameService;
import com.ruchij.api.web.middleware.Authenticator;
import com.ruchij.api.web.requests.NewGameRequest;
import com.ruchij.api.web.responses.PaginatedResponse;
import com.ruchij.api.web.responses.PingResponse;
import com.ruchij.api.web.responses.SseEvent;
import com.ruchij.api.web.responses.WebSocketResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.ws;

public class GameRoute implements EndpointGroup {
    private static final Logger logger = LoggerFactory.getLogger(GameRoute.class);

    private final GameService gameService;
    private final Authenticator authenticator;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Clock clock;
    private final Map<String, ScheduledFuture<?>> pingScheduledFutures = new ConcurrentHashMap<>();

    public GameRoute(
        GameService gameService,
        AuthenticationService authenticationService,
        ScheduledExecutorService scheduledExecutorService,
        Clock clock
    ) {
        this.gameService = gameService;
        this.authenticator = new Authenticator(authenticationService);
        this.scheduledExecutorService = scheduledExecutorService;
        this.clock = clock;
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

            path("/user", () -> {
                this.authenticator.get((user, context) -> {
                    int offset = context.queryParamAsClass("offset", Integer.class).getOrDefault(0);
                    int limit = context.queryParamAsClass("limit", Integer.class).getOrDefault(10);

                    List<PendingGame> pendingGames =
                        this.gameService.getPendingGamesByPlayerId(user.id(), limit, offset);

                    context.status(HttpStatus.OK)
                        .json(new PaginatedResponse<>(pendingGames, offset, limit));
                });
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

        path("/user", () -> {
            this.authenticator.get((user, context) -> {
                int offset = context.queryParamAsClass("offset", Integer.class).getOrDefault(0);
                int limit = context.queryParamAsClass("limit", Integer.class).getOrDefault(10);

                List<Game> games = this.gameService.getUnfinishedGamesByPlayerId(user.id(), limit, offset);

                context.status(HttpStatus.OK)
                    .json(new PaginatedResponse<>(games, offset, limit));
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

            this.authenticator.sse("/updates", (user, sseClient) -> {
                String gameId = sseClient.ctx().pathParam("gameId");

                try {
                    String registrationId = this.gameService.registerForUpdates(
                        gameId,
                        move -> {
                            sseClient.sendEvent(SseEvent.MOVE_UPDATE.name(), move);
                        },
                        winner -> {
                            sseClient.sendEvent(SseEvent.MOVE_UPDATE.name(), winner);
                        }
                    );

                    logger.info("userId={} connected to SSE game updates for gameId={} registrationId={}",
                        user.id(),
                        gameId,
                        registrationId
                    );

                    Runnable closeConnection = () -> {
                        logger.info("Removing SSE for userId={} gameId={} registrationId={}",
                            user.id(),
                            gameId,
                            registrationId
                        );

                        this.gameService.unregisterForUpdates(registrationId);

                        Optional.ofNullable(this.pingScheduledFutures.remove(registrationId))
                            .ifPresent(scheduledFuture -> scheduledFuture.cancel(true));

                        sseClient.close();
                    };

                    this.scheduledExecutorService.scheduleAtFixedRate(
                        () -> {
                            logger.debug(
                                "Sending SSE ping for userId={} gameId={} registrationId={}",
                                user.id(),
                                gameId,
                                registrationId
                            );

                            try {
                                sseClient.sendEvent(SseEvent.PING.name(),
                                    new PingResponse(
                                        user.id(),
                                        user.username(),
                                        this.clock.instant()
                                    )
                                );

                                logger.debug(
                                    "Sent SSE ping for userId={} gameId={} registrationId={}",
                                    user.id(),
                                    gameId,
                                    registrationId
                                );
                            } catch (Exception exception) {
                                logger.info(
                                    "Error sending SSE ping for userId={} gameId={} registrationId={}",
                                    user.id(),
                                    gameId,
                                    registrationId,
                                    exception
                                );

                                closeConnection.run();
                            }

                        },
                        0,
                        10,
                        TimeUnit.SECONDS
                    );

                    sseClient.onClose(closeConnection);
                } catch (ResourceNotFoundException resourceNotFoundException) {
                    logger.error("Unable to find gameId={}", gameId, resourceNotFoundException);
                    sseClient.sendEvent(SseEvent.NOT_FOUND.name(), resourceNotFoundException.getMessage());
                    sseClient.close();
                }
            });

            ws("/updates", ws -> {
                ws.onConnect(wsConnectContext -> {
                    wsConnectContext.session.setIdleTimeout(Duration.of(30, ChronoUnit.MINUTES));

                    User user = this.authenticator.authenticate(wsConnectContext);
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

                    Runnable closeConnection = () -> {
                        logger.info(
                            "Removing WebSocket for userId={} gameId={} registrationId={}",
                            user.id(),
                            gameId,
                            registrationId
                        );

                        this.gameService.unregisterForUpdates(registrationId);

                        Optional.ofNullable(pingScheduledFutures.remove(registrationId))
                            .ifPresent(scheduledFuture -> {
                                scheduledFuture.cancel(false);
                            });

                        wsConnectContext.closeSession();
                    };

                    logger.info(
                        "userId={} connected to WebSocket game updates for gameId={} registrationId={}",
                        user.id(),
                        gameId,
                        registrationId
                    );

                    ScheduledFuture<?> pingScheduledFuture = this.scheduledExecutorService.scheduleAtFixedRate(
                        () -> {
                            logger.debug(
                                "Sending WebSocket ping for userId={} gameId={} registrationId={}",
                                user.id(),
                                gameId,
                                registrationId
                            );

                            try {
                                wsConnectContext.send(
                                    new WebSocketResponse<>(WebSocketResponse.Type.PING,
                                        new PingResponse(
                                            user.id(),
                                            user.username(),
                                            this.clock.instant()
                                        )
                                    ));
                                logger.debug(
                                    "Sent WebSocket ping for userId={} gameId={} registrationId={}",
                                    user.id(),
                                    gameId,
                                    registrationId
                                );
                            } catch (Exception exception) {
                                logger.info(
                                    "Error sending WebSocket ping for userId={} gameId={} registrationId={} error={}",
                                    user.id(),
                                    gameId,
                                    registrationId,
                                    exception.getMessage()
                                );
                                closeConnection.run();
                            }
                        },
                        0,
                        10,
                        TimeUnit.SECONDS
                    );

                    pingScheduledFutures.put(registrationId, pingScheduledFuture);

                    ws.onError(wsErrorContext -> {
                        logger.error(
                            "Error in game updates for userId={} gameId={} registrationId={}",
                            user.id(),
                            gameId,
                            registrationId,
                            wsErrorContext.error()
                        );

                        closeConnection.run();
                    });

                    ws.onClose(wsCloseContext -> {
                        closeConnection.run();
                    });
                });
            });
        });
    }
}
