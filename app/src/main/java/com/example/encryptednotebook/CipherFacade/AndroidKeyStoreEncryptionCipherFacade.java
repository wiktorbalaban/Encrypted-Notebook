package com.example.encryptednotebook.CipherFacade;

import android.content.SharedPreferences;

import com.example.encryptednotebook.Cipher.AndroidKeyStoreCipher;
import com.example.encryptednotebook.Cipher.CipherException;
import com.example.encryptednotebook.InitialVector.SharedPreferencesInitialVector;
import com.example.encryptednotebook.SecretKey.AndroidKeyStoreSecretKeyProvider;

public class AndroidKeyStoreEncryptionCipherFacade implements RandomizedEncryptionCipherFacade {

    private AndroidKeyStoreCipher cipher;
    private SharedPreferencesInitialVector iv;

    public AndroidKeyStoreEncryptionCipherFacade(SharedPreferences prefs) {
        AndroidKeyStoreSecretKeyProvider secretKeyProvider = new AndroidKeyStoreSecretKeyProvider();
        this.cipher = new AndroidKeyStoreCipher(secretKeyProvider);
        this.iv = new SharedPreferencesInitialVector(prefs);
    }

    @Override
    public String encrypt(String message, String key) throws CipherFacadeException {
        try {
            String encryptedMessage = cipher.encrypt(message);
            iv.save(cipher.getIv(), key);
            return encryptedMessage;
        } catch (CipherException e) {
            e.printStackTrace();
            throw new CipherFacadeException();
        }
    }

    @Override
    public String decrypt(String encryptedMessage, String key) throws CipherFacadeException {
        try {
            byte[] iv = this.iv.load(key);
            cipher.setIv(iv);
            return cipher.decrypt(encryptedMessage);
        } catch (CipherException e) {
            e.printStackTrace();
            throw new CipherFacadeException();
        }
    }
}
