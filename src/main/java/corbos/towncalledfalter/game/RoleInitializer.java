package corbos.towncalledfalter.game;

import corbos.towncalledfalter.game.roles.Guardian;
import corbos.towncalledfalter.game.roles.Innocent;
import corbos.towncalledfalter.game.roles.Role;
import corbos.towncalledfalter.game.roles.Seer;
import corbos.towncalledfalter.game.roles.Villager;
import corbos.towncalledfalter.game.roles.Wolf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class RoleInitializer {

    public static List<Role> makeRoles(int count) {

        ArrayList<Role> roles = new ArrayList<>();

        roles.add(new Seer());
        roles.add(new Guardian());
        roles.add(new Innocent());

        HashSet<Player> sharedVotes = new HashSet<>();
        for (int i = 0; i < count / 4; i++) {
            roles.add(new Wolf(sharedVotes));
        }

        int villagerCount = count - roles.size();
        for (int i = 0; i < villagerCount; i++) {
            roles.add(new Villager());
        }

        Collections.shuffle(roles, Randomizer.getRandom());
        return roles;
    }
}
