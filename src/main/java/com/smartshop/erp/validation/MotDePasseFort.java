package com.smartshop.erp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valide qu'un mot de passe respecte une politique minimale professionnelle :
 * au moins 8 caracteres, une majuscule, une minuscule, un chiffre.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MotDePasseFortValidator.class)
@Documented
public @interface MotDePasseFort {
    String message() default "Le mot de passe doit contenir au moins 8 caracteres, "
            + "une majuscule, une minuscule et un chiffre";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
