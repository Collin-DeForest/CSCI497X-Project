package com.raven.passwordmanager.model;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

// Handles encrypting and decrypting the vault file using AES-256 GCM
public class VaultEncryption {

    // File where the encrypted passwords are saved
    private static final String VAULT_FILE = "vault.txt";

    // GCM authentication tag length in bits
    private static final int GCM_TAG_LENGTH = 128;

    // IV length in bytes, required for AES GCM
    private static final int IV_LENGTH = 12;

    // Encrypt all password entries and save to vault.txt
    public static void encrypt(List<PasswordEntry> entries, byte[] key) throws Exception{
        // Convert all entries to plain text lines separated by |
        StringBuilder sb = new StringBuilder();
        for(PasswordEntry entry : entries){
            sb.append(entry.getSite()).append("|")
            .append(entry.getUsername()).append("|")
            .append(entry.getPassword()).append("\n");
        }

        // Generate a random IV for this encryption
        byte[] iv = generateIV();

        // Set up the AES-256 GCM cipher for encryption
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        // Encrypt the data
        byte[] encryptedData = cipher.doFinal(sb.toString().getBytes());

        // Save IV on line 1 and encrypted data on line 2
        try(PrintWriter writer = new PrintWriter(new FileWriter(VAULT_FILE))){
            writer.println(Base64.getEncoder().encodeToString(iv));
            writer.println(Base64.getEncoder().encodeToString(encryptedData));
        }
    }

    // Read vault.txt, check the GCM seal and decrypt back into PasswordEntry objects
    public static List<PasswordEntry> decrypt(byte[] key) throws Exception{
        List<PasswordEntry> entries = new ArrayList<>();

        // If no vault file exists yet just return an empty list
        if(!new File(VAULT_FILE).exists()){
            return entries;
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(VAULT_FILE))){
            // Read IV from line 1 and encrypted data from line 2
            byte[] iv = Base64.getDecoder().decode(reader.readLine());
            byte[] encryptedData = Base64.getDecoder().decode(reader.readLine());

            // Set up AES-256 GCM cipher for decryption
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            // Decrypt and parse each line back into PasswordEntry objects
            String decryptedData = new String(cipher.doFinal(encryptedData));
            for(String line : decryptedData.split("\n")){
                String[] parts = line.split("\\|");
                if(parts.length == 3){
                    entries.add(new PasswordEntry(parts[0], parts[1], parts[2]));
                }
            }
        }
        return entries;
    }

    // Generate a random 12 byte IV for AES GCM
    private static byte[] generateIV(){
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }
}
