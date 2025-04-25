package com.ecommerce.application.constant;

public class RegexPatternConstant {

    private RegexPatternConstant() {}

    public static final String ZIP_CODE_PATTERN = "^[0-9]{6}$";
    public static final String CONTACT_NUMBER_PATTERN = "^[0-9]{10}$";
    public static final String PASSWORD_UPPERCASE = ".*[A-Z].*";
    public static final String PASSWORD_LOWERCASE = ".*[a-z].*";
    public static final String PASSWORD_DIGIT = ".*\\d.*";
    public static final String PASSWORD_SPECIAL_CHAR = ".*[\\W_].*";
    public static final String PASSWORD_LENGTH = "^.{8,15}$";
}
