package com.example.encryptednotebook;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

class Cipher {

    private static final String CIPHER_TYPE = "AES/CBC/PKCS5PADDING";

    private final String salt;
    private SecretKey secretKey;
    private final String initialVector;

    Cipher(String salt, String initialVector, String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.salt = salt;
        this.initialVector = initialVector;
        secretKey = generateKey(password);
    }

    private SecretKey generateKey(String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] saltBytes;
        try {
            saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        } catch (NullPointerException e) {
            e.printStackTrace();
            saltBytes = "".getBytes();
        }
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), saltBytes, 10000, 256);
        return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(keySpec).getEncoded(), "AES");
    }

    String encryptString(String message)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(initialVector.getBytes(StandardCharsets.UTF_8));
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] utfMessageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = cipher.doFinal(utfMessageBytes);
        return Base64.getEncoder().encodeToString(cipherText);
    }

    String decryptString(String message)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(initialVector.getBytes(StandardCharsets.UTF_8));
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] messageBytes = Base64.getDecoder().decode(message);
        byte[] decryptedBytes = cipher.doFinal(messageBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
