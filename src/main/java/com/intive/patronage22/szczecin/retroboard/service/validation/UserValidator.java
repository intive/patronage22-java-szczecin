package com.intive.patronage22.szczecin.retroboard.service.validation;

import com.intive.patronage22.szczecin.retroboard.exception.DisplayNameFormatException;
import com.intive.patronage22.szczecin.retroboard.exception.EmailFormatException;
import com.intive.patronage22.szczecin.retroboard.exception.PasswordFormatException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserValidator {

    public static void validate(final String email, final String password, final String displayName) {
        if (!isEmailValid(email)) {
            throw new EmailFormatException();
        }

        if (!isPasswordValid(password)) {
            throw new PasswordFormatException();
        }

        if (!isDisplayNameValid(displayName)) {
            throw new DisplayNameFormatException();
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
