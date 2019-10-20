package corbos.towncalledfalter.service;

public class Result<T> extends Response {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public static <T> Result<T> invalid(String message) {
        Result<T> result = new Result<>();
        result.addInvalidError(message);
        return result;
    }

    public static <T> Result<T> notFound(String message) {
        Result<T> result = new Result<>();
        result.addNotFoundError(message);
        return result;
    }

    public static <T> Result<T> success(T value) {
        Result<T> result = new Result<>();
        result.setValue(value);
        return result;
    }

}
