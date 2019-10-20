package corbos.towncalledfalter.network;

public class AckMessage extends BaseMessage {

    private final String gameCode;
    private final String playerName;
    private final boolean moderator;

    public AckMessage(String gameCode, String playerName, boolean isModerator) {
        super(MessageType.ACK);

        this.gameCode = gameCode;
        this.playerName = playerName;
        this.moderator = isModerator;
    }

    public String getGameCode() {
        return gameCode;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isModerator() {
        return moderator;
    }

}
