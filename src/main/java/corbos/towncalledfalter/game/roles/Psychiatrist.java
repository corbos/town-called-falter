package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Ability;
import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Prompt;

public class Psychiatrist extends Role {

    public Psychiatrist() {
        super(Alignment.GOOD, Alignment.GOOD);
    }

    @Override
    public String getName() {
        return "Psychiatrist";
    }

    @Override
    public String getDescription() {
        return "The Psychiatrist diagnoses other players. "
                + "They can tell if a player's perceived alignment matches "
                + "their actual alignment.";
    }

    @Override
    public void queueNight(Game game, Player player, boolean firstNight) {
        Prompt p = new Prompt("Who would you like to diagnose?", Ability.INSPECT, 1);
        queue(p);
    }

    @Override
    public void processMove(Move m, Game game, Player player) {

        if (!moveIsMatch(m, game)) {
            return;
        }

        Player patient = m.getPlayers().get(0);
        Role r = patient.getRole();
        player.addMessage(
                String.format(
                        "%s %s conflicted.",
                        patient.getName(),
                        r.getVisibleAlignment() == r.getActualAlignment() ? "is NOT" : "IS"
                ));

        dequeue();
    }

}
