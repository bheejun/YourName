package com.sparta.yourname.util;

import com.sparta.yourname.exception.CustomError;

import java.util.regex.Pattern;

public class ValidateUtil {

    // username의 정규식 패턴
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9]{4,10}$");

    // password의 정규식 패턴
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,15}$");

    // username 유효성 검사
    // username 유효성 검사
    public static boolean isValidUsername(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new CustomError(CustomErrorMessage.WRONG_ID_FORMAT);
        }
        return true;
    }

    // password 유효성 검사
    public static boolean isValidPassword(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new CustomError(CustomErrorMessage.WRONG_PASSWORD_FORMAT);
        }
        return true;
    }
}