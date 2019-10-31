package corbos.towncalledfalter.game;

import corbos.towncalledfalter.game.roles.Role;
import corbos.towncalledfalter.game.roles.Seer;
import corbos.towncalledfalter.game.roles.Villager;
import corbos.towncalledfalter.game.roles.Wolf;
import corbos.towncalledfalter.service.Validation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Game {

    private final String code;
    private final Player moderator;
    private ArrayList<Player> players = new ArrayList<>();
    private GameStatus status = GameStatus.JOINABLE;

    // nomination plumbing
    private final ArrayList<Player> possibleNominators = new ArrayList<>();
    private final ArrayList<Player> possibleNominated = new ArrayList<>();
    private Player nominator;
    private Player nominated;

    // voting plumbing
    private final HashMap<Player, Move> votes = new HashMap<>();

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

        // moves are only valid for specific statuses
        if (!m.getType().isValid(status)) {
            return MoveResult.INVALID_GAME_STATUS;
        }

        // try JOIN first since it's the only move where an
        // existing player is _not_ required or permitted.
        if (m.getType() == MoveType.JOIN) {
            return join(m.getPlayerName());
        }

        // then everything that requires an existing player
        Player player = getPlayer(m.getPlayerName());
        if (player == null) {
            return MoveResult.NOT_AUTHORIZED;
        }

        // moves that require moderator
        if (m.getType().requiresModerator() && player != moderator) {
            return MoveResult.NOT_AUTHORIZED;
        }

        switch (m.getType()) {
            case SETUP:
                return setup();
            case START:
                return start(m);
            case KILL:
                return kill(m);
            case NOMINATE:
                return nominate(m, player);
            case VOTE:
                return vote(m, player);
            case USE_ABILITY:
                return useAbility(m, player);
            default:
                return MoveResult.INVALID_STATE;
        }

    }

    /* 
    Move handlers    
     */
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

        makeNight(true);

        return MoveResult.SUCCESS;
    }

    private MoveResult kill(Move m) {

        // one and only one person can be killed at a time
        if (m.getNames().size() != 1
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

    private MoveResult useAbility(Move m, Player player) {

        player.getRole().processMove(m, this, player);

        // everyone is done
        // currently can only use ability at night 
        // so this can only switch to day
        if (players.stream()
                .allMatch(p -> p.getRole().currentPrompt() == null)) {
            initNomination();
            status = GameStatus.DAY_NOMINATE;
            checkWin();
        }

        return MoveResult.SUCCESS;
    }

    private MoveResult nominate(Move m, Player player) {

        // dead or player has already nominated
        if (player.getStatus() == PlayerStatus.DEAD
                || !possibleNominators.contains(player)) {
            return MoveResult.NOT_AUTHORIZED;
        }

        if (m.getNames().size() > 1) {
            return MoveResult.INVALID_STATE;
        }

        // this is a "no nomination" move
        // player cannot change their mind later
        // ultimately should be some sort of success status, 
        // but don't want a UI repaint for now
        if (m.getNames().isEmpty()) {

            possibleNominators.remove(player);

            // everyone passed, jump to night
            if (possibleNominators.isEmpty()) {
                makeNight(false);
                return MoveResult.SUCCESS;
            }
            return MoveResult.INVALID_STATE;
        }

        Player n = possibleNominated.stream()
                .filter(p -> p.getName().equals(m.getNames().get(0)))
                .findAny()
                .orElse(null);

        // can't nominate this person
        // they don't exist, are dead, or have already been nominated
        if (n == null) {
            return MoveResult.INVALID_STATE;
        }

        possibleNominators.remove(player);
        possibleNominated.remove(n);
        nominator = player;
        nominated = n;

        votes.clear();
        players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ALIVE)
                .forEach(p -> votes.put(p, null));

        status = GameStatus.DAY_VOTE;

        return MoveResult.SUCCESS;
    }

    private MoveResult vote(Move m, Player player) {

        if (!votes.containsKey(player)) {
            return MoveResult.NOT_AUTHORIZED;
        }

        votes.put(player, m);

        if (votes.values().stream().allMatch(v -> v != null)) {

            int threshold = votes.size() / 2 + 1;
            int yeas = votes.values().stream()
                    .mapToInt(v -> v.isConfirmed() ? 1 : 0)
                    .sum();

            // execution
            if (yeas >= threshold) {

                nominated.setStatus(PlayerStatus.DEAD);
                makeNight(false);
                checkWin();

            } else {
                if (possibleNominators.size() > 0 && possibleNominated.size() > 0) {
                    status = GameStatus.DAY_NOMINATE;
                } else {
                    makeNight(false);
                }
            }

            return MoveResult.SUCCESS;
        }

        return MoveResult.INVALID_STATE;
    }

    /* 
     
     */
    private boolean playersMatch(List<String> orderedPlayers) {

        if (orderedPlayers == null
                || orderedPlayers.size() != players.size()) {
            return false;
        }

        return players.stream()
                .allMatch(p -> orderedPlayers.contains(p.getName()));

    }

    private void initNomination() {
        possibleNominators.clear();
        possibleNominated.clear();
        players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ALIVE)
                .forEach(p -> {
                    possibleNominators.add(p);
                    possibleNominated.add(p);
                });
        nominator = null;
        nominated = null;
    }

    private void makeNight(boolean first) {

        // queue actions
        for (Player p : players) {
            if (p.getStatus() == PlayerStatus.ALIVE) {
                p.getRole().queueNight(this, p, first);
            }
        }

        votes.clear();

        // update game status
        status = GameStatus.NIGHT;
    }

    private void checkWin() {

        int good = 0;
        int evil = 0;

        for (Player p : players) {
            if (p.getStatus() == PlayerStatus.ALIVE) {
                if (p.getRole().getAlignment() == Alignment.EVIL) {
                    evil++;
                } else {
                    good++;
                }
            }
        }

        if (good == 0) {
            status = GameStatus.EVIL_WINS;
        } else if (evil == 0) {
            status = GameStatus.GOOD_WINS;
        }
    }

}
