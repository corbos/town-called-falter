package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Ability;
import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.PlayerStatus;
import corbos.towncalledfalter.game.Prompt;
import corbos.towncalledfalter.game.Randomizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Role {

    private final Alignment visibleAlignment;
    private final Alignment actualAlignment;
    private final RoleLabel label;

    private final LinkedList<Prompt> queue = new LinkedList<>();

    private final ArrayList<Player> answers = new ArrayList<>();

    private int promptVersion;

    public Role(RoleLabel label,
            Alignment visibleAlignment, Alignment actualAlignment) {
        this.label = label;
        this.visibleAlignment = visibleAlignment;
        this.actualAlignment = actualAlignment;
    }

    public final Alignment getVisibleAlignment() {
        return visibleAlignment;
    }

    public final Alignment getActualAlignment() {
        return actualAlignment;
    }

    public Prompt currentPrompt() {
        return queue.peek();
    }

    protected void queue(Prompt m) {
        m.setVersion(promptVersion++);
        queue.add(m);
    }

    protected Prompt dequeue() {
        return queue.remove();
    }

    public final String getName() {
        return label.getName();
    }

    public final String getDescription() {
        return label.getDescription();
    }

    public final RoleLabel getLabel() {
        return label;
    }

    /**
     * Each night, the game queues actions for special roles. That makes it easy
     * to determine if someone has a special role because they're clickity-clicking
     * on their device. To prevent that, we prompt players without special roles
     * to answer a simple question.
     * We give them an incentive: answer correctly or die.
     * That keeps everyone focused and makes it harder to ascertain roles
     * via user input. Problem, maybe, solved. :shrug:
     *
     * @param game
     * @param player
     * @param firstNight
     */
    public void queueNight(Game game, Player player, boolean firstNight) {

        switch (Randomizer.getRandom().nextInt(5)) {
            case 0:
                queueAdjacent(game);
                break;
            case 1:
                queueStartsWith(game);
                break;
            case 2:
                queuePick(game, 1);
                break;
            case 3:
                queuePick(game, 2);
                break;
            case 4:
                queueSelf(player);
                break;
        }

    }

    protected boolean moveIsMatch(Move m, Game game) {

        Prompt current = currentPrompt();

        boolean isMatch = current != null
                && current.getAbility() == m.getAbility()
                && ((current.isDismissable() && m.getNames().isEmpty())
                || current.getCount() == m.getNames().size());

        if (isMatch) {
            for (String name : m.getNames()) {
                Player p = game.getPlayer(name);
                if (p == null) {
                    isMatch = false;
                    break;
                }
                m.add(p);
            }
        }

        return isMatch;
    }

    /**
     * In each role with specific powers, three things happen:
     * 1.Confirm the move matches the currently queued move.
     * 2. Do your thing.
     * 3. dequeue
     *
     * Only step 2 is unique per role so we make steps 1 and 3 a shared implementation.
     *
     * @param m
     * @param game
     * @param player
     * @return
     */
    public final Move processMove(Move m, Game game, Player player) {

        if (!moveIsMatch(m, game)) {
            return null;
        }

        // hole-in-the-middle
        Move result = onMoveSuccess(m, game, player);

        dequeue();

        return result;

    }

    public Move onMoveSuccess(Move m, Game game, Player player) {
        Player p = m.getPlayers().get(0);
        if (!answers.contains(p)) {
            player.setStatus(PlayerStatus.DEAD);
        }
        answers.clear();
        return null;
    }

    private void queueAdjacent(Game game) {

        List<Player> players = game.getPlayers();
        int size = players.size();
        int index = Randomizer.getRandom().nextInt(size);

        String msg = String.format("Who sits next to %s?",
                players.get(index).getName());

        Prompt ping = new Prompt(msg, Ability.NONE, 1);
        ping.setCanSelectSelf(true);
        queue(ping);

        answers.add(players.get((index + 1) % size));
        answers.add(players.get((index - 1 + size) % size));
    }

    private void queueStartsWith(Game game) {

        List<Player> players = game.getPlayers();
        int index = Randomizer.getRandom().nextInt(players.size());

        char firstLetter = players.get(index).getName().charAt(0);

        String msg = String.format(
                "Pick one player whose names starts with %s.",
                firstLetter);

        char firstUpper = Character.toUpperCase(firstLetter);

        Prompt ping = new Prompt(msg, Ability.NONE, 1);
        ping.setCanSelectSelf(true);
        queue(ping);

        players.stream()
                .filter(p -> Character.toUpperCase(p.getName().charAt(0)) == firstUpper)
                .forEach(answers::add);
    }

    private void queuePick(Game game, int count) {

        List<Player> players = game.getPlayers();
        HashSet<Integer> indices = new HashSet<>();

        do {
            indices.add(Randomizer.getRandom().nextInt(players.size()));
        } while (indices.size() < count);

        for (int index : indices) {
            answers.add(players.get(index));
        }

        String names = String.join(
                " and ",
                answers.stream()
                        .map(Player::getName)
                        .collect(Collectors.toList())
        );

        String msg = String.format("Choose %s.", names);

        Prompt ping = new Prompt(msg, Ability.NONE, count);
        ping.setCanSelectSelf(true);
        queue(ping);
    }

    private void queueSelf(Player player) {
        Prompt ping = new Prompt("Choose yourself.", Ability.NONE, 1);
        ping.setCanSelectSelf(true);
        queue(ping);
        answers.add(player);
    }

}
