package corbos.towncalledfalter.game;

import corbos.towncalledfalter.game.roles.Role;
import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String name;
    private PlayerStatus status = PlayerStatus.ALIVE;
    private Role role;
    private final ArrayList<String> messages = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void addMessage(String msg) {
        messages.add(msg);
    }

}
