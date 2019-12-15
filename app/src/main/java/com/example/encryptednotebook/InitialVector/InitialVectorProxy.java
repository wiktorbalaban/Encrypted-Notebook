package com.example.encryptednotebook.InitialVector;

public interface InitialVectorProxy {
    byte[] get(String key);

    void set(byte[] iv, String key);
}
