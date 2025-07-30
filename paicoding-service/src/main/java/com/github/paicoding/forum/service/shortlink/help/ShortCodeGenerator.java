package com.github.paicoding.forum.service.shortlink.help;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class ShortCodeGenerator {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE62_LENGTH = BASE62.length();
    private static final int HASH_LENGTH = 5;


    private static final Cache<String, Boolean> existingShortCodes = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build();
    public static String generateShortCode(String longUrl) throws NoSuchAlgorithmException {
        String shortCode = generateHash(longUrl);
        final int MAX_ATTEMPTS = 10;
        int attempts = 0;
        while (existingShortCodes.getIfPresent(shortCode) != null) {
            if (attempts >= MAX_ATTEMPTS) {
                throw new RuntimeException("生成唯一短链接代码失败，已尝试 " + MAX_ATTEMPTS + " 次");
            }
            shortCode = generateHash(longUrl + System.nanoTime());
            attempts++;
        }

        existingShortCodes.put(shortCode, Boolean.TRUE);
        return shortCode;
    }

    private static String generateHash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes());
        StringBuilder hashString = new StringBuilder();

        for (int i = 0; i < hash.length && hashString.length() < ShortCodeGenerator.HASH_LENGTH; i++) {
            int index = (hash[i] & 0xFF) % BASE62_LENGTH;
            hashString.append(BASE62.charAt(index));
        }

        return hashString.toString();
    }

    public static void main(String[] args) {
        try {
            String longUrl = "http://example.com";
            String shortCode = generateShortCode(longUrl);
            System.out.println("The First Short code for " + longUrl + " is " + shortCode);

            String repeatShortCode = generateShortCode(longUrl);
            System.out.println("The Second Short code for " + longUrl + " is " + repeatShortCode);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}