package com.example.encryptednotebook.CipherFacade;

public interface RandomizedEncryptionCipherFacade {
    String encrypt(String message, String key) throws CipherFacadeException;

    String decrypt(String encryptedMessage, String key) throws CipherFacadeException;
}
