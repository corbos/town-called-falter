package corbos.towncalledfalter.service;

public class ActionRequest extends GameRequest {

    private RequestType type;

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

}
