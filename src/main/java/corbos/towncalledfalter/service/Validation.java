package corbos.towncalledfalter.service;

public class Validation {

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank();
    }
}
