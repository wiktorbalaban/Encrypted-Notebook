package com.example.encryptednotebook.CipherFacade;

public interface RandomizedEncryptionCipherFacade {
    void encrypt(String message, String key) throws CipherFacadeException;

    void decrypt(String encryptedMessage, String key) throws CipherFacadeException;

    void setEncryptFinishEventListener(EncryptFinishEventListener encryptFinishEventListener);

    void setDecryptFinishEventListener(DecryptFinishEventListener decryptFinishEventListener);

    interface DecryptFinishEventListener {
        void onDecryptFinishEvent(String decryptedMessage);
    }

    interface EncryptFinishEventListener {
        void onEncryptFinishEvent(String decryptedMessage);
    }
}
