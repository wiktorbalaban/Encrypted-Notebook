package com.example.encryptednotebook.CipherFacade;

public interface RandomizedEncryptionCipherFacade {
    String encrypt(String text, String key);

    String decrypt(String encryptedText, String key);
}
