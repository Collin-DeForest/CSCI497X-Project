package com.raven.passwordmanager.model;

import java.security.SecureRandom;

/*
 Cryptographically secure password generator using SecureRandom.
 Configure character sets and length via GeneratorOptions.
 */
public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";
    private static final String SYMBOLS   = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    private final SecureRandom random = new SecureRandom();

    public static class GeneratorOptions {
        private int length = 16;
        private boolean uppercase = true;
        private boolean lowercase = true;
        private boolean digits = true;
        private boolean symbols = true;

        // Fluent builder-style setters
        public GeneratorOptions length(int length) {
            if (length < 4 || length > 128)
                throw new IllegalArgumentException("Length must be between 4 and 128.");
            this.length = length;
            return this;
        }
        public GeneratorOptions uppercase(boolean v) { this.uppercase = v; return this; }
        public GeneratorOptions lowercase(boolean v) { this.lowercase = v; return this; }
        public GeneratorOptions digits(boolean v)    { this.digits    = v; return this; }
        public GeneratorOptions symbols(boolean v)   { this.symbols   = v; return this; }

        public int getLength()       { return length; }
        public boolean useUppercase() { return uppercase; }
        public boolean useLowercase() { return lowercase; }
        public boolean useDigits()    { return digits; }
        public boolean useSymbols()   { return symbols; }
    }

    /*
      Generates a secure password from the given options.
     
      @param options configuration for the generated password
      @return the generated password string
      @throws IllegalArgumentException if no character sets are selected
     */
    public String generate(GeneratorOptions options) {
        StringBuilder pool = new StringBuilder();
        StringBuilder required = new StringBuilder();

        if (options.useUppercase()) { pool.append(UPPERCASE); required.append(randomChar(UPPERCASE)); }
        if (options.useLowercase()) { pool.append(LOWERCASE); required.append(randomChar(LOWERCASE)); }
        if (options.useDigits())    { pool.append(DIGITS);    required.append(randomChar(DIGITS));    }
        if (options.useSymbols())   { pool.append(SYMBOLS);   required.append(randomChar(SYMBOLS));   }

        if (pool.length() == 0)
            throw new IllegalArgumentException("At least one character set must be selected.");

        String poolStr = pool.toString();
        char[] password = new char[options.getLength()];

        // Place guaranteed characters
        int req = Math.min(required.length(), options.getLength());
        for (int i = 0; i < req; i++) {
            password[i] = required.charAt(i);
        }

        // Fill remainder from pool
        for (int i = req; i < options.getLength(); i++) {
            password[i] = poolStr.charAt(random.nextInt(poolStr.length()));
        }

        // Fisher-Yates shuffle
        for (int i = password.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = password[i];
            password[i] = password[j];
            password[j] = tmp;
        }

        return new String(password);
    }

    // Convenience overload with default options.
    public String generate() {
        return generate(new GeneratorOptions());
    }

    private char randomChar(String charset) {
        return charset.charAt(random.nextInt(charset.length()));
    }
}