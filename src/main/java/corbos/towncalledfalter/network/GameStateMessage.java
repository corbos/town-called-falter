package corbos.towncalledfalter.network;

import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.GameStatus;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Prompt;
import corbos.towncalledfalter.game.roles.Role;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GameStateMessage extends BaseMessage {

    private final Game game;
    private final Player player;
    private final HashMap<String, Boolean> connected;

    public GameStateMessage(
            Game g,
            Player p,
            HashMap<String, Boolean> playerConnected) {
        super(MessageType.GAME_STATE);

        game = g;
        player = p;
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

    public Role getRole() {
        return player.getRole();
    }

    public Prompt getPrompt() {
        if (player.getRole() != null) {
            return player.getRole().currentPrompt();
        }
        return null;
    }

    public List<String> getPlayerMessages() {
        return player.getMessages();
    }

    // nomination-stuff
    public boolean getCanNominate() {
        return game.canNominate(player);
    }

    public List<String> getPossibleNominations() {
        return game.getPossibleNominations().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());
    }

    public String getNominator() {
        if (game.getNominator() != null) {
            return game.getNominator().getName();
        }
        return null;
    }

    public String getNominated() {
        if (game.getNominated() != null) {
            return game.getNominated().getName();
        }
        return null;
    }

    // vote-stuff
    public boolean getCanVote() {
        return game.canVote(player);
    }

    private PlayerState playerToState(Player player) {
        return new PlayerState(
                player,
                connected.getOrDefault(player.getName(), Boolean.FALSE)
        );
    }
}
