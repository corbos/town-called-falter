package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Role;

public class Villager extends Role {

    public Villager() {
        super(Alignment.GOOD);
    }

    @Override
    public String getName() {
        return "Villager";
    }

    @Override
    public String getDescription() {
        return "You are a plain old boring Villager. Sorry.";
    }

    @Override
    public void queueNight(Game game, Player player, boolean firstNight) {
    }

}
