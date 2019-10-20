package corbos.towncalledfalter.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Game {

    private final String code;
    private final Player moderator;
    private final HashMap<String, Player> players = new HashMap<>();
    private GameStatus status = GameStatus.JOINABLE;

    public Game(String code, String moderatorName) {
        this.code = code;
        moderator = new Player(moderatorName);
        players.put(moderator.getName(), moderator);
    }

    public String getCode() {
        return code;
    }

    public GameStatus getStatus() {
        return status;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public Player getPlayer(String playerName) {
        return players.get(playerName);
    }

    public boolean isModerator(String playerName) {
        return moderator.getName().equals(playerName);
    }

    public boolean join(String playerName) {

        if (status != GameStatus.JOINABLE || players.containsKey(playerName)) {
            return false;
        }

        players.put(playerName, new Player(playerName));
        return true;
    }

    public boolean startSetup(String playerName) {

        if (status != GameStatus.JOINABLE || !isModerator(playerName)) {
            return false;
        }

        status = GameStatus.SETUP;
        return true;
    }
}
