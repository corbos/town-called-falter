package corbos.towncalledfalter.service;

import java.util.ArrayList;
import java.util.List;

public class ActionRequest extends GameRequest {

    private ActionType type;
    private List<String> names = new ArrayList<>();

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

}
