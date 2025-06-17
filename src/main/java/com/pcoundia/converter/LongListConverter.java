package com.pcoundia.converter;

import org.apache.commons.lang.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class LongListConverter implements AttributeConverter<List<Long>, String> {

    private static final String SPLIT_CHAR = ",";
    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        return attribute != null ?  StringUtils.join(attribute.stream().map(Object::toString).collect(Collectors.toList()), SPLIT_CHAR) : "";
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        return dbData != null && !StringUtils.isEmpty(dbData) ? Arrays.stream(dbData.split(SPLIT_CHAR)).map(Long::parseLong).collect(Collectors.toList()) : Collections.emptyList();
    }
}
