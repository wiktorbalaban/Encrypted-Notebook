package com.example.encryptednotebook;

import android.content.Context;
import android.preference.PreferenceManager;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Cipher {
    public static SecretKey generateKey(String password, Context ctx)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        //new PBEKeySpec(password, salt, 10, 128)
        String saltString = PreferenceManager.getDefaultSharedPreferences(ctx).getString("SALT", null);
        byte[] saltBytes;
        try {
            saltBytes = saltString.getBytes();
        } catch (NullPointerException e) {
            e.printStackTrace();
            saltBytes = "".getBytes();
        }
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), saltBytes, 10, 256);
        return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(keySpec).getEncoded(), "AES");
    }

    public static String encryptMsg(String message, SecretKey secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        /* Encrypt the message. */
        javax.crypto.Cipher cipher = null;
        cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5PADDING");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secret, ivSpec);
        byte[] utfMessageBytes = message.getBytes(StandardCharsets.UTF_8);
        //byte[] base64MessageBytes = Base64.getUrlDecoder().decode(utfMessageBytes);
        byte[] cipherText = cipher.doFinal(utfMessageBytes);
        String result = Base64.getEncoder().encodeToString(cipherText);
        return result;
    }

    public static String decryptMsg(String message, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        javax.crypto.Cipher cipher = null;
        cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5PADDING");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secret, ivSpec);
        byte[] messageBytes =  Base64.getDecoder().decode(message);//message.getBytes(StandardCharsets.UTF_8);
        byte[] decryptedBytes = cipher.doFinal(messageBytes);
        String decryptString = new String(decryptedBytes, StandardCharsets.UTF_8);
        return decryptString;
    }
}
