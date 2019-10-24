package corbos.towncalledfalter.game;

import java.util.ArrayList;
import java.util.List;

public class Move {

    private String playerName;
    private MoveType type;
    private List<String> names = new ArrayList<>();

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

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

}
