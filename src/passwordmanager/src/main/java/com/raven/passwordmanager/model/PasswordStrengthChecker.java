package com.raven.passwordmanager.model;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

public class PasswordStrengthChecker {

    private final Zxcvbn zxcvbn = new Zxcvbn();

    public static class StrengthResult {
        private final int score;
        private final String label;
        private final boolean hasMinLength;
        private final boolean hasGoodLength;
        private final boolean hasUppercase;
        private final boolean hasLowercase;
        private final boolean hasDigit;
        private final boolean hasSymbol;
        private final boolean noRepetition;
        private final boolean notCommon;

        public StrengthResult(int score, String label,
                              boolean hasMinLength, boolean hasGoodLength,
                              boolean hasUppercase, boolean hasLowercase,
                              boolean hasDigit, boolean hasSymbol,
                              boolean noRepetition, boolean notCommon) {
            this.score = score;
            this.label = label;
            this.hasMinLength = hasMinLength;
            this.hasGoodLength = hasGoodLength;
            this.hasUppercase = hasUppercase;
            this.hasLowercase = hasLowercase;
            this.hasDigit = hasDigit;
            this.hasSymbol = hasSymbol;
            this.noRepetition = noRepetition;
            this.notCommon = notCommon;
        }

        public int getScore() { return score; }
        public String getLabel() { return label; }
        public boolean hasMinLength() { return hasMinLength; }
        public boolean hasGoodLength() { return hasGoodLength; }
        public boolean hasUppercase() { return hasUppercase; }
        public boolean hasLowercase() { return hasLowercase; }
        public boolean hasDigit() { return hasDigit; }
        public boolean hasSymbol() { return hasSymbol; }
        public boolean noRepetition() { return noRepetition; }
        public boolean notCommon() { return notCommon; }
    }

    public StrengthResult evaluate(String password) {
        if (password == null || password.isEmpty())
            return new StrengthResult(0, "Very Weak", false, false, false, false, false, false, false, false);

        Strength strength = zxcvbn.measure(password);

        int score = strength.getScore() * 25;

        String label = switch (strength.getScore()) {
            case 0 -> "Very Weak";
            case 1 -> "Weak";
            case 2 -> "Fair";
            case 3 -> "Strong";
            default -> "Very Strong";
        };

        return new StrengthResult(
            score, label,
            password.length() >= 8,
            password.length() >= 14,
            password.chars().anyMatch(Character::isUpperCase),
            password.chars().anyMatch(Character::isLowerCase),
            password.chars().anyMatch(Character::isDigit),
            password.chars().anyMatch(c -> !Character.isLetterOrDigit(c)),
            !password.matches(".*(.)\\1{2,}.*"),
            strength.getScore() >= 2
        );
    }
}