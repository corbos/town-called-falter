package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Prompt;
import java.util.LinkedList;

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

    public abstract void processMove(Move m, Game game, Player player);

    public abstract void queueNight(Game game, Player player, boolean firstNight);

    protected boolean moveIsMatch(Move m) {

        Prompt current = currentPrompt();

        return current != null
                && current.getAbility() == m.getAbility()
                && current.getCount() == m.getNames().size();
    }

}
