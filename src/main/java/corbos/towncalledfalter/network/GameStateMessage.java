package corbos.towncalledfalter.network;

import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.GameStatus;
import corbos.towncalledfalter.game.Player;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GameStateMessage extends BaseMessage {

    private final Game game;
    private final HashMap<String, Boolean> connected;

    public GameStateMessage(
            Game g,
            Player p,
            HashMap<String, Boolean> playerConnected) {
        super(MessageType.GAME_STATE);

        game = g;
        connected = playerConnected;
    }

    public GameStatus getGameStatus() {
        return game.getStatus();
    }

    public List<PlayerState> getPlayers() {
        return game.getPlayers().stream()
                .map(this::playerToState)
                .collect(Collectors.toList());
    }

    private PlayerState playerToState(Player player) {
        return new PlayerState(
                player,
                connected.getOrDefault(player.getName(), Boolean.FALSE)
        );
    }
}
