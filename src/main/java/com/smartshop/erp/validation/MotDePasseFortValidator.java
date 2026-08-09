package com.smartshop.erp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class MotDePasseFortValidator implements ConstraintValidator<MotDePasseFort, String> {

    private static final Pattern MAJUSCULE = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULE = Pattern.compile("[a-z]");
    private static final Pattern CHIFFRE = Pattern.compile("[0-9]");
    private static final int LONGUEUR_MIN = 8;

    @Override
    public boolean isValid(String motDePasse, ConstraintValidatorContext context) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MIN) return false;
        return MAJUSCULE.matcher(motDePasse).find()
                && MINUSCULE.matcher(motDePasse).find()
                && CHIFFRE.matcher(motDePasse).find();
    }
}
