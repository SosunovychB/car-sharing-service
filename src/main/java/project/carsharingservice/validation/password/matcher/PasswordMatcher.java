package project.carsharingservice.validation.password.matcher;

import jakarta.validation.*;

import java.lang.annotation.*;

@Constraint(validatedBy = PasswordMatcherValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatcher {
    String message() default "Passwords don't match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
