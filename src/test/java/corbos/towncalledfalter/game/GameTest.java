package corbos.towncalledfalter.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    public GameTest() {

    }

    @Test
    public void testSeer() {

        Game game = makeGame(5);

        assertEquals(GameStatus.NIGHT, game.getStatus());

        Move m = new Move("klee", MoveType.USE_ABILITY);
        m.setAbility(Ability.INTUIT);
        m.setNames(Arrays.asList("sarah"));

        game.move(m);

        assertTrue(game.getPlayer("klee").getMessages().size() == 1);
        assertEquals(Ability.NOMINATE,
                game.getPlayer("klee").getRole().currentPrompt().getAbility());
    }

    @Test
    public void testWolves() {
        Game game = makeGame(10);
        assertEquals(GameStatus.NIGHT, game.getStatus());
        assertEquals(1, game.getPlayer("rupert").getMessages().size());
        assertEquals(1, game.getPlayer("klee").getMessages().size());
    }

    private Game makeGame(int playerCount) {

        String moderator = "sarah";

        String[] names = {"umo", "basil", "rupert", "klee",
            "ming", "orn", "adamo", "brigida", "max",
            "chance", "mads", "berni", "dalt", "sumter",
            "wing", "fornsworth", "winnie", "carla", "soup",
            "dorn", "piet", "zuzu"
        };

        Random rand = new Random(1);
        Game result = new Game("code", moderator, rand);

        for (int i = 0; i < playerCount - 1; i++) {
            result.move(new Move(names[i], MoveType.JOIN));
        }

        result.move(new Move(moderator, MoveType.SETUP));

        List<String> sortedNames = result.getPlayers().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());

        Collections.shuffle(sortedNames, rand);

        Move startMove = new Move(moderator, MoveType.START);
        startMove.getNames().addAll(sortedNames);
        result.move(startMove);

        return result;
    }

}
