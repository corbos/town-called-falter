package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Alignment;

public class Villager extends Role {

    public Villager() {
        super(Alignment.GOOD, Alignment.GOOD);
    }

    @Override
    public String getName() {
        return "Villager";
    }

    @Override
    public String getDescription() {
        return "You are a plain old boring Villager. Sorry.";
    }

}
