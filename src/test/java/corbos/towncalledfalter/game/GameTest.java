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

    /* 5 person game
    sarah -> moderator, villager
    umo -> villager
    basil -> villager
    rupert -> wolf
    klee - seer
     */
    @Test
    public void testVillagersWin() {

        Game game = makeGame(5);
        assertEquals(GameStatus.NIGHT, game.getStatus());

        Move m = new Move("klee", MoveType.USE_ABILITY);
        m.setAbility(Ability.INSPECT);
        m.setNames(Arrays.asList("sarah"));
        game.move(m);

        assertEquals(GameStatus.DAY_NOMINATE, game.getStatus());

        // non-existent player
        game.move(makeNomination("sarah", "nope"));
        assertEquals(GameStatus.DAY_NOMINATE, game.getStatus());

        // valid nomination
        game.move(makeNomination("sarah", "klee"));
        assertEquals(GameStatus.DAY_VOTE, game.getStatus());

        for (Player p : game.getPlayers()) {
            m = new Move(p.getName(), MoveType.VOTE);
            game.move(m);
        }

        assertEquals(GameStatus.DAY_NOMINATE, game.getStatus());

        // sarah can't nominate again    
        game.move(makeNomination("sarah", "umo"));
        assertEquals(GameStatus.DAY_NOMINATE, game.getStatus());

        // klee already received a vote, so he can't be nominated again      
        game.move(makeNomination("umo", "klee"));
        assertEquals(GameStatus.DAY_NOMINATE, game.getStatus());

        // valid nomination
        game.move(makeNomination("umo", "basil"));
        assertEquals(GameStatus.DAY_VOTE, game.getStatus());

        for (Player p : game.getPlayers()) {
            m = new Move(p.getName(), MoveType.VOTE);
            if (!p.getName().equals("basil")) {
                m.setConfirmed(true);
            }
            game.move(m);
        }

        assertEquals(GameStatus.NIGHT, game.getStatus());
        assertEquals(PlayerStatus.DEAD, game.getPlayer("basil").getStatus());

        m = new Move("klee", MoveType.USE_ABILITY);
        m.setAbility(Ability.INSPECT);
        m.setNames(Arrays.asList("umo"));
        game.move(m);

        assertEquals(GameStatus.NIGHT, game.getStatus());

        m = new Move("rupert", MoveType.USE_ABILITY);
        m.setAbility(Ability.KILL);
        m.setNames(Arrays.asList("umo"));
        game.move(m);

        assertEquals(PlayerStatus.DEAD, game.getPlayer("umo").getStatus());
        assertEquals(GameStatus.DAY_NOMINATE, game.getStatus());

        // valid nomination
        game.move(makeNomination("sarah", "rupert"));
        assertEquals(GameStatus.DAY_VOTE, game.getStatus());

        for (Player p : game.getPlayers()) {
            if (p.getStatus() == PlayerStatus.DEAD) {
                continue;
            }
            m = new Move(p.getName(), MoveType.VOTE);
            if (!p.getName().equals("rupert")) {
                m.setConfirmed(true);
            }
            game.move(m);
        }

        assertEquals(PlayerStatus.DEAD, game.getPlayer("rupert").getStatus());
        assertEquals(GameStatus.GOOD_WINS, game.getStatus());
    }

    @Test
    public void testSeer() {

        Game game = makeGame(5);

        assertEquals(GameStatus.NIGHT, game.getStatus());

        Move m = new Move("klee", MoveType.USE_ABILITY);
        m.setAbility(Ability.INSPECT);
        m.setNames(Arrays.asList("sarah"));

        game.move(m);

        assertEquals(1, game.getPlayer("klee").getMessages().size());
        assertNull(game.getPlayer("klee").getRole().currentPrompt());
        assertEquals(GameStatus.DAY_NOMINATE, game.getStatus());

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

        Randomizer.setRandom(new Random(1));
        Game result = new Game("code", moderator);

        for (int i = 0; i < playerCount - 1; i++) {
            result.move(new Move(names[i], MoveType.JOIN));
        }

        result.move(new Move(moderator, MoveType.SETUP));

        List<String> sortedNames = result.getPlayers().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());

        Collections.shuffle(sortedNames, Randomizer.getRandom());

        Move startMove = new Move(moderator, MoveType.START);
        startMove.getNames().addAll(sortedNames);
        result.move(startMove);

        return result;
    }

    private Move makeNomination(String sender, String receiver) {
        Move m = new Move(sender, MoveType.NOMINATE);
        m.setNames(Arrays.asList(receiver));
        return m;
    }

}
