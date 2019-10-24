package corbos.towncalledfalter.network;

import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.PlayerStatus;

public class PlayerState {

    private final Player player;
    private final boolean connected;

    public PlayerState(Player p, boolean connected) {
        player = p;
        this.connected = connected;
    }

    public String getName() {
        return player.getName();
    }

    public boolean isConnected() {
        return connected;
    }

    public PlayerStatus getStatus() {
        return player.getStatus();
    }
}
