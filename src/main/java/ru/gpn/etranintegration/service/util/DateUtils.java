package ru.gpn.etranintegration.service.util;

import ru.gpn.etranintegration.model.etran.message.ValueAttribute;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private DateUtils() {}

    public static String convertToString(LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIME_FORMATTER);
    }

    public static LocalDateTime convertToLocalDateTime(String string) {
        return LocalDateTime.parse(string, DATE_TIME_FORMATTER);
    }

    public static ValueAttribute convertToValueAttribute(LocalDateTime localDateTime) {
        ValueAttribute valueAttribute = new ValueAttribute();
        valueAttribute.setValue(convertToString(localDateTime));
        return valueAttribute;
    }

}
