package com.raven.passwordmanager.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

public class TwoFactor {

    private final TimeBasedOneTimePasswordGenerator totp;
    private final SecretKey key;
    private final String base32Secret;
    private final boolean newlyGenerated;

    public TwoFactor() {
        String storedSecret = loadSecretFromFile();
        if (storedSecret != null) {
            this.newlyGenerated = false;
            // Use the stored secret if it exists
            this.base32Secret = storedSecret;
            byte[] decodedKey = new Base32().decode(this.base32Secret); 
            this.key = new SecretKeySpec(decodedKey, "HmacSHA1");
            this.totp = new TimeBasedOneTimePasswordGenerator();
        } else {
            this.newlyGenerated = true;
            // Generate a new key if no secret is found
            this.totp = new TimeBasedOneTimePasswordGenerator();
            KeyGenerator keyGen;
            try {
                keyGen = KeyGenerator.getInstance(totp.getAlgorithm());
                this.key = keyGen.generateKey();
            } catch (Exception e) {
                throw new RuntimeException("Error generating key", e);
            }

            Base32 base32 = new Base32();
            this.base32Secret = base32.encodeToString(key.getEncoded());

            // Stores new secret key
            saveSecretToFile(this.base32Secret);
        }
    }

    private void saveSecretToFile(String secret) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("two_factor_secret.txt"))) {
            writer.write(secret);
        } catch (IOException e) {
            throw new RuntimeException("Error saving secret to file", e);
        }
    }

    private String loadSecretFromFile() {
        try {
            return new String(Files.readAllBytes(Paths.get("two_factor_secret.txt")));
        } catch (IOException e) {
            return null;
        }
    }

    public String getSecret() {
        return base32Secret;
    }

    public String getOtpAuthURL(String account, String issuer) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, account, base32Secret, issuer
        );
    }

    public boolean isSetupRequired() {
        return newlyGenerated;
    }

    public boolean verify(String code) {
        try {
            int expected = totp.generateOneTimePassword(key, Instant.now());
            return String.format("%06d", expected).equals(code);
        } catch (Exception e) {
            return false;
        }
    }
}