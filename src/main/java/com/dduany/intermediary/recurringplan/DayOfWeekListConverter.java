package com.dduany.intermediary.recurringplan;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

/** Stores the weekday list as "MONDAY,WEDNESDAY,FRIDAY" in a single column. */
@Converter
public class DayOfWeekListConverter implements AttributeConverter<List<DayOfWeek>, String> {

    @Override
    public String convertToDatabaseColumn(List<DayOfWeek> days) {
        if (days == null || days.isEmpty()) return "";
        return String.join(",", days.stream().distinct().sorted().map(DayOfWeek::name).toList());
    }

    @Override
    public List<DayOfWeek> convertToEntityAttribute(String column) {
        if (column == null || column.isBlank()) return List.of();
        return Arrays.stream(column.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(DayOfWeek::valueOf).toList();
    }
}
