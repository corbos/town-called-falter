package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Ability;
import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Prompt;
import java.util.List;

public class Seer extends Role {

    public Seer() {
        super(Alignment.GOOD);
    }

    @Override
    public String getName() {
        return "Seer";
    }

    @Override
    public String getDescription() {
        return "The Seer wakes up at night "
                + "and can intuit the alignment of any one player.";
    }

    @Override
    public void queueNight(Game game, Player playter, boolean firstNight) {
        queue(new Prompt("Choose a player to inspect.", Ability.INTUIT, 1));
    }

    @Override
    public void processMove(Move m, Game game, Player player) {

        if (!moveIsMatch(m)) {
            return;
        }

        // no player 
        Player p = game.getPlayer(m.getNames().get(0));
        if (p == null) {
            return;
        }

        dequeue();

        player.addMessage(
                String.format("%s is %s.",
                        p.getName(),
                        p.getRole().getAlignment()));
    }

}
