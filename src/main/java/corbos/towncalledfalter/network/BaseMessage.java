package corbos.towncalledfalter.network;

public abstract class BaseMessage {

    private final MessageType type;

    public BaseMessage(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }
}
