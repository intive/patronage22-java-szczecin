package com.intive.patronage22.szczecin.retroboard.service.validation;

import com.intive.patronage22.szczecin.retroboard.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserValidator {

    public static void validate(final String email, final String password, final String displayName) {
        if (!isEmailValid(email)) {
            throw new BadRequestException("Email format not valid");
        }

        if (!isPasswordValid(password)) {
            throw new BadRequestException("Password cannot be empty");
        }

        if (!isDisplayNameValid(displayName)) {
            throw new BadRequestException("DisplayName cannot be empty");
        }
    }

    private static boolean isDisplayNameValid(final String displayName) {
        return StringUtils.hasText(displayName);
    }

    private static boolean isPasswordValid(final String password) {
        return StringUtils.hasText(password) && password.length() >= 6;
    }

    private static boolean isEmailValid(final String emailAddress) {
        final String regexPattern = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";

        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}