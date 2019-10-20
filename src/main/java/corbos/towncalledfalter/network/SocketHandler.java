package corbos.towncalledfalter.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.service.ActionRequest;
import corbos.towncalledfalter.service.GamePool;
import corbos.towncalledfalter.service.GameRequest;
import corbos.towncalledfalter.service.Result;
import java.io.IOException;
import java.util.HashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// 
@Component
public class SocketHandler extends TextWebSocketHandler {

    private final GamePool pool;

    // socketId -> gameCode
    private final HashMap<String, String> sockToGame
            = new HashMap<>();
    // gameCode -> socket -> playerName
    private final HashMap<String, HashMap<WebSocketSession, String>> gameToSocks
            = new HashMap<>();

    private final Object lock = new Object();

    private final ObjectMapper mapper = new ObjectMapper();

    public SocketHandler(GamePool pool) {
        this.pool = pool;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        String gameCode = sockToGame.get(session.getId());

        // locks
        disconnect(session);

        if (gameCode != null) {
            Result<Game> result = pool.getGame(gameCode);
            if (!result.hasError()) {
                // locks
                sendGameState(result.getValue());
            }
        }

        System.out.printf("WS Closed. Id:%s, Status: %s\n", session.getId(), status);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("WS Connection Established. Id:" + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        System.out.printf("WS Transport Err. Id:%s, Ex: %s\n",
                session.getId(), ex.getMessage());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {

        ActionRequest request = parseRequest(message.getPayload());
        if (request == null) {
            return;
        }

        System.out.printf("WS Message %s: %s\n",
                request.getType(), session.getId());

        switch (request.getType()) {
            case CONNECT:
                connect(session, request);
                break;
            case START_SETUP:
                startSetup(session, request);
                break;
            default:
                System.out.println("WS Message Failure: Unknown Type");
                break;
        }

    }

    private ActionRequest parseRequest(String json) {

        if (json == null || json.length() == 0) {
            System.out.println("WS Message Failure: null or empty payload");
            return null;
        }

        try {
            return mapper.readValue(json, ActionRequest.class);
        } catch (IOException ex) {
            System.out.println("WS Message Failure:" + ex.getMessage());
            return null;
        }
    }

    private void connect(WebSocketSession session, GameRequest request) {

        // locks
        disconnect(session);

        String gameCode = request.getGameCode();

        Result<Game> result = pool.getGame(gameCode);
        if (!result.hasError()) {

            Game g = result.getValue();
            Player p = g.getPlayer(request.getPlayerName());

            // correct game, correct player 
            if (p != null) {
                // locks
                register(session, gameCode, p.getName());
                sendAck(session, g, p);
                // locks
                sendGameState(g);
            }
        }
    }

    private void register(WebSocketSession session, String gameCode, String playerName) {
        synchronized (lock) {
            // 1. Map game to player
            var players = gameToSocks.get(gameCode);
            if (players == null) {
                players = new HashMap<>();
                gameToSocks.put(gameCode, players);
            }

            for (String name : players.values()) {
                // whoops, someone already claimed the character
                if (name.equals(playerName)) {
                    return;
                }
            }

            players.put(session, playerName);

            // 2. Map socket to game
            sockToGame.put(session.getId(), gameCode);
        }
    }

    private void disconnect(WebSocketSession session) {
        synchronized (lock) {
            String gameCode = sockToGame.get(session.getId());
            if (gameCode != null) {

                var players = gameToSocks.get(gameCode);
                if (players != null) {
                    players.remove(session);
                    if (players.isEmpty()) {
                        gameToSocks.remove(gameCode);
                    }
                }

                sockToGame.remove(session.getId());
            }
        }
    }

    private void startSetup(WebSocketSession session, ActionRequest request) {

        String gameCode = sockToGame.get(session.getId());
        if (gameCode == null || !gameCode.equals(request.getGameCode())) {
            return;
        }

        Result<Game> result = pool.startSetup(
                gameCode,
                request.getPlayerName());

        if (result.hasError()) {
            return;
        }

        Game game = result.getValue();
        sendGameState(game);

    }

    private void sendAck(WebSocketSession session, Game g, Player p) {
        AckMessage msg = new AckMessage(
                g.getCode(), p.getName(), g.isModerator(p.getName()));
        send(session, msg);
    }

    private void sendGameState(Game game) {

        synchronized (lock) {

            var sockToPlayer = gameToSocks.get(game.getCode());
            if (sockToPlayer == null) {
                return;
            }

            var entries = sockToPlayer.entrySet();

            HashMap<String, Boolean> playerConnected = new HashMap<>();
            for (Player p : game.getPlayers()) {
                playerConnected.put(p.getName(), Boolean.FALSE);
            }
            for (var entry : entries) {
                playerConnected.put(entry.getValue(), Boolean.TRUE);
            }

            for (var entry : entries) {
                Player p = game.getPlayer(entry.getValue());
                if (p != null) {
                    var msg = new GameStateMessage(game, p, playerConnected);
                    send(entry.getKey(), msg);
                }
            }

        }
    }

    private void send(WebSocketSession session, BaseMessage msg) {

        String json;
        try {
            json = mapper.writeValueAsString(msg);
        } catch (JsonProcessingException ex) {
            System.out.println("WS Json Failure:" + ex.getMessage());
            return;
        }

        TextMessage tm = new TextMessage(json);
        try {
            if (session.isOpen()) {
                session.sendMessage(tm);
            }
        } catch (IOException ex) {
            System.out.println("WS Message Failure:" + ex.getMessage());
        } catch (IllegalStateException ex) {
            System.out.println("WS Illegal State:" + ex.getMessage());
        }
    }

}
