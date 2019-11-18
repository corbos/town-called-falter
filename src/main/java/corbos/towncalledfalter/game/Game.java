package corbos.towncalledfalter.game;

import corbos.towncalledfalter.game.roles.Role;
import corbos.towncalledfalter.service.Validation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Game {

    private final String code;
    private final Player moderator;
    private ArrayList<Player> players = new ArrayList<>();
    private GameStatus status = GameStatus.JOINABLE;

    // nomination plumbing
    private final ArrayList<Player> possibleNominators = new ArrayList<>();
    private final ArrayList<Player> possibleNominations = new ArrayList<>();
    private Player nominator;
    private Player nominated;

    // voting plumbing
    private final HashMap<Player, Move> votes = new HashMap<>();

    // per-player night status
    private final HashMap<Player, List<Ability>> night = new HashMap<>();

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

    public boolean canNominate(Player player) {
        return status == GameStatus.DAY_NOMINATE
                && player.getStatus() == PlayerStatus.ALIVE
                && possibleNominators.contains(player);
    }

    public List<Player> getPossibleNominations() {
        return possibleNominations;
    }

    public Player getNominator() {
        return nominator;
    }

    public Player getNominated() {
        return nominated;
    }

    public boolean canVote(Player player) {
        return status == GameStatus.DAY_VOTE
                && player.getStatus() == PlayerStatus.ALIVE
                && votes.get(player) == null;
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

        if (players.size() < 4) { // need at least 4 players
            return MoveResult.INVALID_STATE;
        }

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
        List<Role> roles = RoleInitializer.makeRoles(players.size());
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

        checkWin();

        return MoveResult.SUCCESS;
    }

    private MoveResult useAbility(Move m, Player player) {

        Move result = player.getRole().processMove(m, this, player);

        // some abilities affect others so we must track over time
        // PROTECT stops KILL, etc...
        // then make decisions after everyone is done.
        if (result != null) {
            for (Player p : result.getPlayers()) {
                List<Ability> nightEffects = night.get(p);
                if (nightEffects == null) {
                    nightEffects = new ArrayList<>();
                }
                nightEffects.add(result.getAbility());
                night.put(p, nightEffects);
            }
        }

        // everyone is done
        // currently can only use ability at night 
        // so this can only switch to day
        if (players.stream()
                .allMatch(p -> p.getRole().currentPrompt() == null)) {

            applyNightEffects();

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
        if (m.getNames().isEmpty()) {

            possibleNominators.remove(player);

            // everyone passed, jump to night
            if (possibleNominators.isEmpty()) {
                makeNight(false);
            }

            return MoveResult.SUCCESS;
        }

        Player n = possibleNominations.stream()
                .filter(p -> p.getName().equals(m.getNames().get(0)))
                .findAny()
                .orElse(null);

        // can't nominate this person
        // they don't exist, are dead, or have already been nominated
        if (n == null) {
            return MoveResult.INVALID_STATE;
        }

        possibleNominators.remove(player);
        possibleNominations.remove(n);
        nominator = player;
        nominated = n;

        votes.clear();
        players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ALIVE)
                .forEach(p -> votes.put(p, null));

        // a nomination is automatically a "yes" vote
        Move vote = new Move(player.getName(), MoveType.VOTE);
        vote.getNames().add(n.getName());
        vote.setConfirmed(true);
        votes.put(player, vote);

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
                if (possibleNominators.size() > 0 && possibleNominations.size() > 0) {
                    nominator = null;
                    nominated = null;
                    status = GameStatus.DAY_NOMINATE;
                } else {
                    makeNight(false);
                }
            }

        }

        return MoveResult.SUCCESS;
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
        possibleNominations.clear();
        players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ALIVE)
                .forEach(p -> {
                    possibleNominators.add(p);
                    possibleNominations.add(p);
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

    private void applyNightEffects() {

        for (Player p : night.keySet()) {

            List<Ability> nightEffects = night.get(p);
            if (nightEffects == null) {
                continue;
            }

            boolean hasProtection = false;
            boolean hasKill = false;
            for (Ability a : nightEffects) {
                if (a == Ability.PROTECT) {
                    hasProtection = true;
                } else if (a == Ability.KILL) {
                    hasKill = true;
                }
            }

            if (hasKill && !hasProtection) {
                p.setStatus(PlayerStatus.DEAD);
            }
        }

        night.clear();
    }

    private void checkWin() {

        int good = 0;
        int evil = 0;

        for (Player p : players) {
            if (p.getStatus() == PlayerStatus.ALIVE) {
                if (p.getRole().getActualAlignment() == Alignment.EVIL) {
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
