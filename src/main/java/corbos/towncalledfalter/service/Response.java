package corbos.towncalledfalter.service;

import java.util.ArrayList;
import java.util.List;

public class Response {

    private final ArrayList<String> errorMessages = new ArrayList<>();
    private ResponseStatus status = ResponseStatus.SUCCESS;

    public boolean hasError() {
        return errorMessages.size() > 0;
    }

    public List<String> getErrors() {
        return new ArrayList<>(errorMessages);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void addNotFoundError(String message) {
        status = ResponseStatus.NOT_FOUND;
        addError(message);
    }

    public void addInvalidError(String message) {
        status = ResponseStatus.INVALID;
        addError(message);
    }

    private void addError(String message) {
        errorMessages.add(message);
    }
}
