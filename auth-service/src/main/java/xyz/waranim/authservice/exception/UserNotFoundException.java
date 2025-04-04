package xyz.waranim.authservice.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("Пользователь с email " + email + " не найден");
    }
}
