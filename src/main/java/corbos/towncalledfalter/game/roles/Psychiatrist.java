package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Ability;
import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Prompt;

public class Psychiatrist extends Role {

    public Psychiatrist() {
        super(RoleLabel.PSYCHIATRIST, Alignment.GOOD, Alignment.GOOD);
    }

    @Override
    public void queueNight(Game game, Player player, boolean firstNight) {
        Prompt p = new Prompt("Who would you like to diagnose?", Ability.INSPECT, 1);
        queue(p);
    }

    @Override
    public Move onMoveSuccess(Move m, Game game, Player player) {

        Player patient = m.getPlayers().get(0);
        Role r = patient.getRole();
        player.addMessage(
                String.format(
                        "%s %s conflicted.",
                        patient.getName(),
                        r.getVisibleAlignment() == r.getActualAlignment() ? "is NOT" : "IS"
                ));

        return null;

    }

}
