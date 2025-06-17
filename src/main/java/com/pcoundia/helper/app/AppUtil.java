package com.pcoundia.helper.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public final class AppUtil {

    private AppUtil() {
    }

    public static Calendar getToday() {
        Calendar dateTodayCalendar = Calendar.getInstance();
        dateTodayCalendar.setTime(new Date());
        dateTodayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dateTodayCalendar.set(Calendar.MINUTE, 0);
        dateTodayCalendar.set(Calendar.SECOND, 0);
        dateTodayCalendar.set(Calendar.MILLISECOND, 0);
        return dateTodayCalendar;
    }

    public static Calendar getNow() {
        Calendar dateNowCalendar = Calendar.getInstance();
        dateNowCalendar.setTime(new Date());
        return dateNowCalendar;
    }

    public static String getNowToStringWithHour() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        return format.format(new Date().getTime());
    }

    public static String getTodayDateToString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(AppUtil.getToday().getTime());
    }

    public static String getDateOnlyToString(Date date) {
        if (date == null)
            return null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static String getTodayPrefApplicantToString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        return format.format(AppUtil.getToday().getTime());
    }

    public static String generateRandomString(Integer length) {
        if (length == null) {
            length = 10;
        }
        // choose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    public static Map<String, Object> convertToMap(String str) {
        try {
            return (new ObjectMapper()).readValue(str, Map.class);
        } catch (JsonProcessingException exception) {
            log.warn("Erreur lors de la conversion en map du json {}", str);
        }
        return null;
    }

    public static String generateRandomString(Integer length, List<String> charsAuthorised) {
        if (length == null) {
            length = 10;
        }
        String AlphaNumericString = "";
        if (charsAuthorised == null || charsAuthorised.isEmpty()) {
            // choose a Character random from this String
            AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    + "0123456789"
                    + "abcdefghijklmnopqrstuvxyz";
        } else {
            if (charsAuthorised.contains("majs"))
                AlphaNumericString += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            if (charsAuthorised.contains("mins"))
                AlphaNumericString += "abcdefghijklmnopqrstuvxyz";
            if (charsAuthorised.contains("numerics"))
                AlphaNumericString += "0123456789";
        }

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    public static boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authorityStr) {
        if (authorities != null) {
            for (GrantedAuthority authority: authorities) {
                if (authority.getAuthority().equals(authorityStr))
                    return true;
            }
        }
        return false;
    }

    public static Class<?> getFieldType(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field.getType();
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Field not found", e);
        }
    }

    public static boolean isValidJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
