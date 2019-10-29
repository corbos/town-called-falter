package corbos.towncalledfalter.game;

import corbos.towncalledfalter.game.roles.Seer;
import corbos.towncalledfalter.game.roles.Villager;
import corbos.towncalledfalter.game.roles.Wolf;
import corbos.towncalledfalter.service.Validation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game {

    private final String code;
    private final Player moderator;
    private List<Player> players = new ArrayList<>();
    private GameStatus status = GameStatus.JOINABLE;
    private final Random rand;

    public Game(String code, String moderatorName) {
        this(code, moderatorName, new Random());
    }

    public Game(String code, String moderatorName, Random rand) {
        this.code = code;
        moderator = new Player(moderatorName);
        players.add(moderator);
        this.rand = rand;
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
            case USE_ABILITY:
                return useAbility(m);
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

        // proper order
        players = sorted;

        // assign roles
        ArrayList<Role> roles = new ArrayList<>();
        roles.add(new Seer());
        roles.add(new Wolf());

        if (players.size() > 5) {
            roles.add(new Wolf());
        }

        int villagerCount = players.size() - roles.size();
        for (int i = 0; i < villagerCount; i++) {
            roles.add(new Villager());
        }
        Collections.shuffle(roles, rand);
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setRole(roles.get(i));
        }

        // queue actions
        for (Player p : players) {
            p.getRole().queueNight(this, p, true);
        }

        // update game status
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

    private MoveResult useAbility(Move m) {

        Player player = getPlayer(m.getPlayerName());
        if (player == null) {
            return MoveResult.INVALID_STATE;
        }

        player.getRole().processMove(m, this, player);

        // everyone is done
        if (players.stream()
                .allMatch(p -> p.getRole().currentPrompt() == null)) {
            if (status == GameStatus.DAY) {
                // queue night actions
                for (Player p : players) {
                    p.getRole().queueNight(this, p, false);
                }
                status = GameStatus.NIGHT;
            } else {
                // queue day actions
                for (Player p : players) {
                    p.getRole().queueDay(this, p);
                }
                status = GameStatus.DAY;
            }

        }

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
