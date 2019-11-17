package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Ability;
import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Prompt;

public class Guardian extends Role {

    private Player lastGuarded;

    public Guardian() {
        super(RoleLabel.GUARDIAN, Alignment.GOOD, Alignment.GOOD);
    }

    @Override
    public Move onMoveSuccess(Move m, Game game, Player player) {

        // first night, consciousness check.
        if (m.getAbility() == Ability.NONE) {
            return super.onMoveSuccess(m, game, player);
        }

        // dismissed the prompt
        if (m.getPlayers().isEmpty()) {
            lastGuarded = null;
            return null;
        }

        // if the Guardian selects the same player twice in a row, protection
        // fails and it's silently ignored.
        // they've also lost the ability to guard that player 
        // on the following night.
        if (m.getPlayers().get(0) != lastGuarded) {
            lastGuarded = m.getPlayers().get(0);
            return m;
        }

        return null;

    }

    @Override
    public void queueNight(Game game, Player player, boolean firstNight) {

        if (firstNight) {
            super.queueNight(game, player, firstNight);
        } else {
            Prompt prompt = new Prompt(
                    "Who do you want to protect?", Ability.PROTECT, 1);
            prompt.setDismissable(true);
            queue(prompt);
        }
    }

}
