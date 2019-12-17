package com.example.encryptednotebook.SecretKey;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AndroidKeyStoreSecretKeyProvider implements SecretKeyProvider {
    private static final String keyStoreAlias = "AndroidKeyStore";

    @Override
    public SecretKey get(String keyAlias) throws SecretKeyException {
        try {
            KeyStore keyStore = KeyStore.getInstance(keyStoreAlias);
            keyStore.load(null);
            if (!keyStore.isKeyEntry(keyAlias)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreAlias); // 1
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setUserAuthenticationRequired(true) // 2 requires lock screen, invalidated if lock screen is disabled
                        .setUserAuthenticationValidityDurationSeconds(-1) // 3 only available x seconds from password authentication. -1 requires finger print - every time
                        .setRandomizedEncryptionRequired(true) // 4 different ciphertext for same plaintext on each call
                        .build();
                keyGenerator.init(keyGenParameterSpec);
                keyGenerator.generateKey();
            }
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry)
                    keyStore.getEntry(keyAlias, null);
            return secretKeyEntry.getSecretKey();
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException
                | NoSuchProviderException | InvalidAlgorithmParameterException
                | UnrecoverableEntryException e) {
            e.printStackTrace();
            throw new SecretKeyException();
        }
    }
}
