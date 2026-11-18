package com.ticketbooking.service;

import java.util.Locale;

public final class TextSearch {

    private TextSearch() {
    }

    public static boolean isBlank(String query) {
        return query == null || query.isBlank();
    }

    public static boolean matches(String query, String... fields) {
        if (isBlank(query)) {
            return true;
        }
        StringBuilder text = new StringBuilder();
        for (String field : fields) {
            if (field != null) {
                text.append(normalize(field)).append(' ');
            }
        }
        String haystack = text.toString();
        for (String word : normalize(query).split("\\s+")) {
            if (!word.isEmpty() && !haystack.contains(word)) {
                return false;
            }
        }
        return true;
    }

    public static boolean sameText(String filterValue, String value) {
        if (isBlank(filterValue)) {
            return true;
        }
        return value != null && normalize(value).equals(normalize(filterValue));
    }

    static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replace('ё', 'е').trim();
    }
}
