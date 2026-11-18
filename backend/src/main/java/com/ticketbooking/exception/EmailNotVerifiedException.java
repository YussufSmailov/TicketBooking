package com.ticketbooking.exception;

public class EmailNotVerifiedException extends RuntimeException {

    private final String email;

    public EmailNotVerifiedException(String email) {
        super("Подтвердите email: введите код из письма");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
