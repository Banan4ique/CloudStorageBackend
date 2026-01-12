package ru.netology.cloudStorage.exception;

import org.springframework.security.core.AuthenticationException;
import java.util.*;

public class BadCredentialsException extends AuthenticationException {

    private final Map<String, List<String>> fieldErrors = new HashMap<>();

    public BadCredentialsException() {
        super("Invalid login or password");
        fieldErrors.put("email", Arrays.asList("Неверный email или пароль"));
        fieldErrors.put("password", Arrays.asList("Неверный email или пароль"));
    }

    public BadCredentialsException(String message) {
        super(message);
        fieldErrors.put("email", Arrays.asList(message));
        fieldErrors.put("password", Arrays.asList(message));
    }

    public BadCredentialsException(Map<String, List<String>> fieldErrors) {
        super("Invalid credentials");
        this.fieldErrors.putAll(fieldErrors);
        if (!this.fieldErrors.containsKey("email")) {
            this.fieldErrors.put("email", new ArrayList<>());
        }
        if (!this.fieldErrors.containsKey("password")) {
            this.fieldErrors.put("password", new ArrayList<>());
        }
    }

    public static BadCredentialsException emailNotFound() {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put("email", Arrays.asList("Пользователь с таким email не найден"));
        errors.put("password", new ArrayList<>());
        return new BadCredentialsException(errors);
    }

    public static BadCredentialsException wrongPassword() {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put("email", new ArrayList<>());
        errors.put("password", Arrays.asList("Неверный пароль"));
        return new BadCredentialsException(errors);
    }

    public static BadCredentialsException invalidCredentials() {
        Map<String, List<String>> errors = new HashMap<>();
        errors.put("email", Arrays.asList("Неверный email или пароль"));
        errors.put("password", Arrays.asList("Неверный email или пароль"));
        return new BadCredentialsException(errors);
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}