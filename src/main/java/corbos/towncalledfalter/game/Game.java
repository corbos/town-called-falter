package corbos.towncalledfalter.game;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private final String code;
    private final Player moderator;
    private List<Player> players = new ArrayList<>();
    private GameStatus status = GameStatus.JOINABLE;

    public Game(String code, String moderatorName) {
        this.code = code;
        moderator = new Player(moderatorName);
        players.add(moderator);
    }

    public String getCode() {
        return code;
    }

    public GameStatus getStatus() {
        return status;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public Player getPlayer(String playerName) {
        return players.stream()
                .filter(p -> p.getName().equals(playerName))
                .findAny()
                .orElse(null);
    }

    public boolean isModerator(String playerName) {
        return moderator.getName().equals(playerName);
    }

    public OperationResult join(String playerName) {

        if (status != GameStatus.JOINABLE) {
            return OperationResult.INVALID_GAME_STATUS;
        }

        if (getPlayer(playerName) != null) {
            return OperationResult.INVALID_STATE;
        }

        players.add(new Player(playerName));
        return OperationResult.SUCCESS;
    }

    public OperationResult setup(String playerName) {

        if (status != GameStatus.JOINABLE) {
            return OperationResult.INVALID_GAME_STATUS;
        }

        if (!isModerator(playerName)) {
            return OperationResult.NOT_AUTHORIZED;
        }

        status = GameStatus.SETUP;

        return OperationResult.SUCCESS;
    }

    public OperationResult start(String playerName, List<String> orderedPlayers) {

        if (status != GameStatus.SETUP) {
            return OperationResult.INVALID_GAME_STATUS;
        }

        if (!isModerator(playerName)) {
            return OperationResult.NOT_AUTHORIZED;
        }

        if (!playersMatch(orderedPlayers)) {
            return OperationResult.INVALID_STATE;
        }

        ArrayList<Player> sorted = new ArrayList<>();
        for (String name : orderedPlayers) {
            sorted.add(getPlayer(name));
        }

        players = sorted;
        status = GameStatus.DAY;

        return OperationResult.SUCCESS;
    }

    private boolean playersMatch(List<String> orderedPlayers) {

        if (orderedPlayers == null
                || orderedPlayers.size() != players.size()) {
            return false;
        }

        return players.stream()
                .allMatch(p -> orderedPlayers.contains(p.getName()));

    }

}
