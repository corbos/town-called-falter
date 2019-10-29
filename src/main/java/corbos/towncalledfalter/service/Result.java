package corbos.towncalledfalter.service;

import java.util.ArrayList;
import java.util.List;

public class Result<T> {

    private final ArrayList<String> errorMessages = new ArrayList<>();
    private ResponseStatus status = ResponseStatus.SUCCESS;
    private T value;

    public boolean hasError() {
        return errorMessages.size() > 0;
    }

    public List<String> getErrors() {
        return new ArrayList<>(errorMessages);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    private void addError(String message) {
        errorMessages.add(message);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public static <T> Result<T> invalid(String message) {
        Result<T> result = new Result<>();
        result.status = ResponseStatus.INVALID;
        result.addError(message);
        return result;
    }

    public static <T> Result<T> notFound(String message) {
        Result<T> result = new Result<>();
        result.status = ResponseStatus.NOT_FOUND;
        result.addError(message);
        return result;
    }

    public static <T> Result<T> success(T value) {
        Result<T> result = new Result<>();
        result.setValue(value);
        return result;
    }

}
