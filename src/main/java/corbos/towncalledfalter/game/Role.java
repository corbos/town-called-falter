package corbos.towncalledfalter.game;

import java.util.LinkedList;
import java.util.List;

public abstract class Role {

    private final Alignment alignment;
    private final LinkedList<Prompt> queue = new LinkedList<>();

    public Role(Alignment alignment) {
        this.alignment = alignment;
    }

    public final Alignment getAlignment() {
        return alignment;
    }

    public Prompt currentPrompt() {
        return queue.peek();
    }

    protected void queue(Prompt m) {
        queue.add(m);
    }

    protected Prompt dequeue() {
        return queue.remove();
    }

    public abstract String getName();

    public abstract String getDescription();

    public void processMove(Move m, Game game, Player player) {

        Prompt current = currentPrompt();

        if (current != null) {

            if (current.getAbility() == Ability.NOMINATE
                    && m.getAbility() == Ability.NOMINATE) {
                handleNomination(m.getNames(), game, player);
            }
        }
    }

    public abstract void queueNight(Game game, Player player, boolean firstNight);

    public void queueDay(Game game, Player player) {
        Prompt nominate = new Prompt("Nominate a player for execution?", Ability.NOMINATE, 1);
        nominate.setDismissable(true);
        queue(nominate);
    }

    private void handleNomination(List<String> names, Game game, Player player) {

        // dismissed/ignored prompt
        if (player.getStatus() == PlayerStatus.DEAD
                || names == null
                || names.isEmpty()) {
            dequeue();
        } else if (names.size() == 1) { // legit nomination

            Player nominee = game.getPlayer(names.get(0));

            // legit player
            if (nominee != null 
                    && player != nominee && 
                    nominee.getStatus() == PlayerStatus.ALIVE) {
            }
        }
    }
}
