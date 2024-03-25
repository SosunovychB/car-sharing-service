package project.carsharingservice.validation.password.matcher;

import jakarta.validation.*;
import project.carsharingservice.dto.registration.*;

import java.util.*;

public class PasswordMatcherValidator
        implements ConstraintValidator<PasswordMatcher, UserRegistrationRequestDto> {
    @Override
    public boolean isValid(UserRegistrationRequestDto requestDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        return requestDto != null
                && requestDto.getPassword() != null
                && requestDto.getRepeatPassword() != null
                && Objects.equals(requestDto.getPassword(), requestDto.getRepeatPassword());
    }
}
