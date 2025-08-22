package core.hackathon02api.auth.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try { return attribute == null ? null : om.writeValueAsString(attribute); }
        catch (Exception e) { return "[]"; }
    }
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try { return dbData == null ? null : om.readValue(dbData, new TypeReference<List<String>>(){}); }
        catch (Exception e) { return Collections.emptyList(); }
    }
}