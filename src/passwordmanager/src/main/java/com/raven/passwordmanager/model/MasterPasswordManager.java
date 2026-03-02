package com.raven.passwordmanager.model;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.io.*;
import java.util.Base64;

// Handles hasing, veryfying and storing the master password
public class MasterPasswordManager {

    // File where we will store the hash value and the salt
    private static final String PASSWORD_FILE = "master.txt";

    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH = 256;

    // Check if a master password has already been created
    public static boolean isSetup(){
        return new File(PASSWORD_FILE).exists();
    }

    // method for creating the master password
    public static void createMasterPassword(String password) throws Exception{
        // Use a salt to make the master password even safer
        byte[] salt = generateSalt();
        byte[] hash = hashPassword(password, salt);

        // Save salt and hash to file
        try(PrintWriter writer = new PrintWriter(new FileWriter(PASSWORD_FILE))){
            writer.println(Base64.getEncoder().encodeToString(salt));
            writer.println(Base64.getEncoder().encodeToString(hash));
        }
    }

    // Check if the entered password matches the masterpassword in master.txt
    public static boolean verifyMasterPassword(String password) throws Exception{
        try(BufferedReader reader = new BufferedReader(new FileReader(PASSWORD_FILE))){
            // Get the salt
            byte[] salt = Base64.getDecoder().decode(reader.readLine());
            byte[] storedHash = Base64.getDecoder().decode(reader.readLine());
            // CHeck if the entered password + salt result in the master password hash
            byte[] enteredHash = hashPassword(password, salt);

            return MessageDigest.isEqual(enteredHash, storedHash);
        }
    }

    // use the master password to create an AES-256 key from our hash value
    public static byte[] deriveKey(String password) throws Exception{
        try(BufferedReader reader = new BufferedReader(new FileReader(PASSWORD_FILE))){
            byte[] salt = Base64.getDecoder().decode(reader.readLine());
            return hashPassword(password, salt);
        }
    }

    // hash the password and salt together using PBKDF2
    private static byte[] hashPassword(String password, byte[] salt) throws Exception{
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }

    // Generate a random 16 byte salt to use with our password
    private static byte[] generateSalt(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
}