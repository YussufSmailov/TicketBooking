package com.ticketbooking.dto;

public final class ValidationRules {

    public static final String EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PHONE = "^\\+?[0-9]{10,15}$";
    public static final String NAME = "^[\\p{L} \\-]+$";
    public static final String PASSWORD = "^(?=.*[A-Za-zА-Яа-яЁё])(?=.*[0-9]).+$";
    public static final String POSTER_URL = "^((https?://|/)\\S+)?$";
    public static final String AGE_RATING = "^(0|6|12|14|16|18|21)\\+$";

    public static final String EMAIL_MESSAGE = "Введите email в формате name@example.com";
    public static final String PHONE_MESSAGE = "Телефон: от 10 до 15 цифр, можно с + в начале";
    public static final String NAME_MESSAGE = "Имя может содержать только буквы, пробелы и дефис";
    public static final String POSTER_URL_MESSAGE = "Ссылка на постер: http://, https:// или путь на сайте, например /images/movies/poster.jpg";
    public static final String PASSWORD_MESSAGE = "Пароль должен содержать буквы и цифры";
    public static final String NAME_SIZE_MESSAGE = "Имя должно быть от 2 до 100 символов";

    private ValidationRules() {
    }
}
