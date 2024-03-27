package project.carsharingservice.validation.password.matcher;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import project.carsharingservice.dto.auth.registration.UserRegistrationRequestDto;

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
