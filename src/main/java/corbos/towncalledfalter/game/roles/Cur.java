package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Alignment;

public class Cur extends Role {

    public Cur() {
        super(Alignment.EVIL, Alignment.GOOD);
    }

    @Override
    public String getName() {
        return "Cur";
    }

    @Override
    public String getDescription() {
        return "The Cur is, let's face it, a bit mangy. "
                + "You appear to be EVIL, but you're actually GOOD. "
                + "In fact, you're a very nice person.";
    }

}
