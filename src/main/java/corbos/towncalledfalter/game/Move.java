package corbos.towncalledfalter.game;

import java.util.ArrayList;
import java.util.List;

public class Move {

    private String playerName;
    private MoveType type;
    private Ability ability;
    private List<String> names = new ArrayList<>();
    private boolean confirmed;

    // empty ctor required for serialization
    public Move() {
    }

    public Move(String playerName, MoveType type) {
        this.playerName = playerName.trim();
        this.type = type;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public MoveType getType() {
        return type;
    }

    public void setType(MoveType type) {
        this.type = type;
    }

    public Ability getAbility() {
        return ability;
    }

    public void setAbility(Ability a) {
        ability = a;
    }

    public List<String> getNames() {
        if (names == null) {
            return new ArrayList<>();
        }
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

}
