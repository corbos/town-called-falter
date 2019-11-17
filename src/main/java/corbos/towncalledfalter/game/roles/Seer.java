package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Ability;
import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Prompt;

public class Seer extends Role {

    public Seer() {
        super(RoleLabel.SEER, Alignment.GOOD, Alignment.GOOD);
    }

    @Override
    public void queueNight(Game game, Player playter, boolean firstNight) {
        queue(new Prompt("Choose a player to inspect.", Ability.INSPECT, 1));
    }

    @Override
    public Move onMoveSuccess(Move m, Game game, Player player) {

        Player p = m.getPlayers().get(0);

        player.addMessage(
                String.format("%s is %s.",
                        p.getName(),
                        p.getRole().getVisibleAlignment()));

        return null;
    }

}
