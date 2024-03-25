package project.carsharingservice.validation.password;

import jakarta.validation.*;

import java.util.regex.*;


public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        return password != null && Pattern.compile(PASSWORD_PATTERN).matcher(password).matches();
    }
}
