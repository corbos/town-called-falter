package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Ability;
import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.PlayerStatus;
import corbos.towncalledfalter.game.Prompt;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Wolf extends Role {

    private static HashSet<Player> killVotes = new HashSet<>();

    public Wolf() {
        super(Alignment.EVIL);
    }

    @Override
    public String getName() {
        return "Werewolf";
    }

    @Override
    public String getDescription() {
        return "You are a werewolf. You kill people at night.";
    }

    @Override
    public void processMove(Move m, Game game, Player player) {

        if (!moveIsMatch(m)) {
            return;
        }

        // no player
        Player p = game.getPlayer(m.getNames().get(0));
        if (p == null) {
            return;
        }

        killVotes.add(p);
        dequeue();

        List<Player> wolves = game.getPlayers().stream()
                .filter(i -> {
                    return i.getRole() instanceof Wolf
                            && i.getStatus() == PlayerStatus.ALIVE;
                })
                .collect(Collectors.toList());

        boolean allVoted = wolves.stream()
                .allMatch(i -> i.getRole().currentPrompt() == null);

        if (allVoted && killVotes.size() == 1) {
            p.setStatus(PlayerStatus.DEAD);
        } else {
            for (Player wolf : wolves) {
                wolf.getRole().queue(
                        new Prompt("Votes were not unanimous. Choose a player to kill.", Ability.KILL, 1));
            }
            killVotes.clear();
        }

    }

    @Override
    public void queueNight(Game game, Player player, boolean firstNight) {

        if (firstNight) {

            List<String> otherEvil = game.getPlayers().stream()
                    .filter(p -> p.getRole().getAlignment() == Alignment.EVIL)
                    .filter(p -> !p.getName().equals(player.getName()))
                    .map(p -> p.getName())
                    .collect(Collectors.toList());

            if (!otherEvil.isEmpty()) {
                String msg = String.format("Evil: %s",
                        String.join(",", otherEvil));
                player.addMessage(msg);
            }

        } else {

            if (!killVotes.isEmpty()) {
                killVotes.clear();
            }

            // kill
            queue(new Prompt("Choose a player to kill.", Ability.KILL, 1));
        }
    }

}
