package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Ability;
import corbos.towncalledfalter.game.Alignment;

public class Innocent extends Role {

    private boolean isNominated = false;

    public Innocent() {
        super(RoleLabel.INNOCENT, Alignment.GOOD, Alignment.GOOD);
    }

    @Override
    public Ability nominate() {
        if (!isNominated) {
            isNominated = true;
            return Ability.KILL;
        }
        return super.nominate();
    }

}
