package com.example.encryptednotebook.Cipher;

public interface Cipher {
    String encrypt(String text);

    String decrypt(String encryptedText);
}
