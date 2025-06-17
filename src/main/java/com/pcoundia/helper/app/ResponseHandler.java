package com.pcoundia.helper.app;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {

    static class MyNullKeySerializer extends JsonSerializer<Object>
    {
        @Override
        public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused)
                throws IOException, JsonProcessingException
        {
            jsonGenerator.writeFieldName("");
        }
    }

    public static ResponseEntity<?> generateResponse(String message, HttpStatus status, Object responseObj){
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status.value());
        map.put("data", responseObj);
        return new ResponseEntity<>(map,status);

    }

    public static ResponseEntity<?> generateResponse(String message, HttpStatus status, Object responseObj, long totalItems, int totalPages) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status.value());
        map.put("data", responseObj);
        map.put("totalItems", totalItems);
        map.put("totalPages", totalPages);
        return new ResponseEntity<>(map,status);
    }
}
