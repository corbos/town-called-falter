package corbos.towncalledfalter.game.roles;

import corbos.towncalledfalter.game.Alignment;
import corbos.towncalledfalter.game.Game;
import corbos.towncalledfalter.game.Move;
import corbos.towncalledfalter.game.Player;
import corbos.towncalledfalter.game.Role;
import java.util.List;
import java.util.stream.Collectors;

public class Wolf extends Role {

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
        // must happen first
        super.processMove(m, game, player);
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
                String msg = String.format("Werewolves: %s",
                        String.join(",", otherEvil));
                player.addMessage(msg);
            }

        } else {
            // kill
        }
    }

}
