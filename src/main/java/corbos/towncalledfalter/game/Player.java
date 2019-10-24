package corbos.towncalledfalter.game;

public class Player {

    private final String name;
    private PlayerStatus status = PlayerStatus.ALIVE;

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
}
