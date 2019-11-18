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

    private final HashSet<Player> killVotes;

    public Wolf(HashSet<Player> sharedVotes) {
        super(RoleLabel.WEREWOLF, Alignment.EVIL, Alignment.EVIL);
        killVotes = sharedVotes;
    }

    @Override
    public Move onMoveSuccess(Move m, Game game, Player player) {

        // first night, the wolf does a consciousness check.
        if (m.getAbility() == Ability.NONE) {
            return super.onMoveSuccess(m, game, player);
        }

        // otherwise they do wolf-y stuff.  
        Player p = m.getPlayers().get(0);

        killVotes.add(p);

        // since our prompt is dequeued after onMoveSuccess,
        // we have to ignore ourselves
        List<Player> wolves = game.getPlayers().stream()
                .filter(i -> {
                    return i.getRole().getLabel() == RoleLabel.WEREWOLF
                            && i.getStatus() == PlayerStatus.ALIVE
                            && i != player;
                })
                .collect(Collectors.toList());

        boolean allVoted = wolves.stream()
                .allMatch(i -> i.getRole().currentPrompt() == null);

        if (!allVoted) {
            return null;
        }

        if (killVotes.size() == 1) {
            return m;
        } else {
            // we ignored ourselves. 
            // now we have to become part of the wolf pack again
            // kinda gross, but ¯\_(ツ)_/¯
            wolves.add(player);
            for (Player wolf : wolves) {
                wolf.getRole().queue(
                        new Prompt("Votes were not unanimous. Choose a player to kill.", Ability.KILL, 1));
            }
            killVotes.clear();
        }

        return null;
    }

    @Override
    public void queueNight(Game game, Player player, boolean firstNight) {

        if (firstNight) {

            super.queueNight(game, player, firstNight);

            List<String> otherEvil = game.getPlayers().stream()
                    .filter(p -> p.getRole().getActualAlignment() == Alignment.EVIL)
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
