package corbos.towncalledfalter.game;

import java.util.Random;

public class Randomizer {

    private static Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    public static void setRandom(Random r) {
        random = r;
    }
}
