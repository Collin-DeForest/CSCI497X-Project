package com.raven.passwordmanager.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/*
 Checks passwords against the Have I Been Pwned Passwords API
 using the k-anonymity model 
 
 The full password is NEVER transmitted. Only the first 5 hex characters
 of the SHA-1 hash are sent; matching is done locally.
 
 API reference: https://haveibeenpwned.com/API/v3#PwnedPasswords
 */
public class HaveIBeenPwnedChecker {

    private static final String API_URL = "https://api.pwnedpasswords.com/range/";
    private static final int    TIMEOUT_MS = 5000;

    public static class PwnedResult {
        private final boolean pwned;
        private final int count;
        private final String errorMessage;

        private PwnedResult(boolean pwned, int count, String errorMessage) {
            this.pwned = pwned;
            this.count = count;
            this.errorMessage = errorMessage;
        }

        public static PwnedResult safe() { return new PwnedResult(false, 0, null); }
        public static PwnedResult pwned(int count) { return new PwnedResult(true, count, null); }
        public static PwnedResult error(String msg) { return new PwnedResult(false, 0, msg); }

        public boolean isPwned() { return pwned; }
        /** Number of times this password appeared in breaches (0 if not pwned). */
        public int getCount() { return count; }
        public boolean isError() { return errorMessage != null; }
        public String getErrorMessage() { return errorMessage; }

        @Override
        public String toString() {
            if (isError())  return "Error: " + errorMessage;
            if (isPwned())  return "Pwned " + count + " time(s) — do not use this password!";
            return "Not found in known breaches.";
        }
    }

   // run this off the EDT since it makes a network call
    public PwnedResult check(String password) {
        if (password == null || password.isEmpty()) {
            return PwnedResult.error("Password must not be empty.");
        }

        try {
            String hash = sha1(password).toUpperCase();
            String prefix = hash.substring(0, 5);
            String suffix = hash.substring(5);

            String responseBody = fetchRange(prefix);
            int count = findSuffix(responseBody, suffix);

            return count > 0 ? PwnedResult.pwned(count) : PwnedResult.safe();

        } catch (Exception e) {
            return PwnedResult.error("Could not reach HIBP API: " + e.getMessage());
        }
    }

    // helper methods

    private String fetchRange(String prefix) throws Exception {
        URL url = new URL(API_URL + prefix);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        // Padding header prevents traffic-analysis side-channel attacks
        conn.setRequestProperty("Add-Padding", "true");
        conn.setRequestProperty("User-Agent", "RavenPasswordManager");

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("HTTP " + status + " from HIBP API");
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    // each line in the response is SUFFIX:COUNT
    private int findSuffix(String responseBody, String suffix) {
        for (String line : responseBody.split("\n")) {
            String[] parts = line.trim().split(":");
            if (parts.length >= 2 && parts[0].equalsIgnoreCase(suffix)) {
                try {
                    return Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    return 1; // Presence confirmed even if count unparseable
                }
            }
        }
        return 0;
    }

    private String sha1(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}