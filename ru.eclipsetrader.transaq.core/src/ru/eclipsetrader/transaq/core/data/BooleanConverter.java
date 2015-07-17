package ru.eclipsetrader.transaq.core.data;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BooleanConverter implements AttributeConverter<Boolean, Integer>{
    @Override
    public Integer convertToDatabaseColumn(Boolean value) {
        if (Boolean.TRUE.equals(value)) {
            return 1;
        } else {
            return 0;
        }
    }
    @Override
    public Boolean convertToEntityAttribute(Integer value) {
        return value == 1;
    }
}