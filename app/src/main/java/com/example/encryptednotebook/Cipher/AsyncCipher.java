package com.example.encryptednotebook.Cipher;

public interface AsyncCipher {
    void encrypt(String message) throws CipherException;

    void decrypt(String encryptedMessage) throws CipherException;

    void setEncryptFinishEventListener(EncryptFinishEventListener encryptFinishEventListener);

    void setDecryptFinishEventListener(DecryptFinishEventListener decryptFinishEventListener);

    interface EncryptFinishEventListener {
        void onEncryptFinishEvent(String encryptedMessage, byte[] iv);
    }

    interface DecryptFinishEventListener {
        void onDecryptFinishEvent(String decryptedMessage);
    }
}
