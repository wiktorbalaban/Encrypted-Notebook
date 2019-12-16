package com.example.encryptednotebook.Cipher;

public interface Cipher {
    String encrypt(String message) throws CipherException;

    String decrypt(String encryptedMessage) throws CipherException;
}
