package corbos.towncalledfalter.game;

import corbos.towncalledfalter.service.Validation;
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

    public synchronized MoveResult move(Move m) {

        // always need to know the player
        if (Validation.isNullOrEmpty(m.getPlayerName())) {
            return MoveResult.NOT_AUTHORIZED;
        }

        // moves that require moderator
        if (m.getType().requiresModerator()
                && !isModerator(m.getPlayerName())) {
            return MoveResult.NOT_AUTHORIZED;
        }

        // moves are only valid for specific statuses
        if (!m.getType().isValid(status)) {
            return MoveResult.INVALID_GAME_STATUS;
        }

        switch (m.getType()) {
            case JOIN:
                return join(m.getPlayerName());
            case SETUP:
                return setup();
            case START:
                return start(m);
            case KILL:
                return kill(m);
            case VOTE:
            case USE_POWER:
            default:
                return MoveResult.INVALID_STATE;
        }

    }

    private MoveResult join(String playerName) {

        if (getPlayer(playerName) != null) {
            return MoveResult.INVALID_STATE;
        }

        players.add(new Player(playerName));
        return MoveResult.SUCCESS;
    }

    private MoveResult setup() {
        status = GameStatus.SETUP;
        return MoveResult.SUCCESS;
    }

    private MoveResult start(Move m) {

        if (!playersMatch(m.getNames())) {
            return MoveResult.INVALID_STATE;
        }

        ArrayList<Player> sorted = new ArrayList<>();
        for (String name : m.getNames()) {
            sorted.add(getPlayer(name));
        }

        players = sorted;
        status = GameStatus.NIGHT;

        return MoveResult.SUCCESS;
    }

    private MoveResult kill(Move m) {

        // one and only one person can be killed at a time
        if (m.getNames() == null
                || m.getNames().size() != 1
                || Validation.isNullOrEmpty(m.getNames().get(0))) {
            return MoveResult.INVALID_STATE;
        }

        // player to-be-killed doesn't exist or is already dead!
        Player candidate = getPlayer(m.getNames().get(0));
        if (candidate == null || candidate.getStatus() == PlayerStatus.DEAD) {
            return MoveResult.INVALID_STATE;
        }

        candidate.setStatus(PlayerStatus.DEAD);

        return MoveResult.SUCCESS;
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
