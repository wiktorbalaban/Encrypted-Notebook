package com.example.encryptednotebook;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

class Cipher {

    private static final String CIPHER_TYPE = "AES/GCM/NoPadding";

    private final String initialVector;

    Cipher(String initialVector) {
        this.initialVector = initialVector;
    }

    public static void generateKey()
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"); // 1
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder("MyKeyAlias",
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                //.setUserAuthenticationRequired(true) // 2 requires lock screen, invalidated if lock screen is disabled
                .setUserAuthenticationValidityDurationSeconds(30) // 3 only available x seconds from password authentication. -1 requires finger print - every time
                .setRandomizedEncryptionRequired(true) // 4 different ciphertext for same plaintext on each call
                .build();
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    String encryptString(String message, SharedPreferences prefs)
            throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, UnrecoverableEntryException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        KeyStore.SecretKeyEntry secretKeyEntry =(KeyStore.SecretKeyEntry)
                keyStore.getEntry("MyKeyAlias", null);
        SecretKey secretKey = secretKeyEntry.getSecretKey();
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);
        GCMParameterSpec ivSpec = new GCMParameterSpec(96, initialVector.getBytes(StandardCharsets.UTF_8));
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv= cipher.getIV();
        prefs.edit().putString(SharedConstants.INITIAL_VECTOR, Base64.getEncoder().encodeToString(iv)).apply();//TODO: trzeba zapisywać iv osovbno dla pass i notatki

        byte[] utfMessageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = cipher.doFinal(utfMessageBytes);
        return Base64.getEncoder().encodeToString(cipherText);
    }

    String decryptString(String message, SharedPreferences prefs)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, KeyStoreException, CertificateException, IOException, UnrecoverableEntryException {

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        KeyStore.SecretKeyEntry secretKeyEntry =(KeyStore.SecretKeyEntry)
                keyStore.getEntry("MyKeyAlias", null);
        SecretKey secretKey = secretKeyEntry.getSecretKey();


        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);
        String iv = prefs.getString(SharedConstants.INITIAL_VECTOR, null);
        GCMParameterSpec ivSpec = new GCMParameterSpec(128, Base64.getDecoder().decode(iv));
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] messageBytes = Base64.getDecoder().decode(message);
        byte[] decryptedBytes = cipher.doFinal(messageBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
