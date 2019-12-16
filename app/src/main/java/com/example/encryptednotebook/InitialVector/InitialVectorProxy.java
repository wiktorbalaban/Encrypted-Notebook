package com.example.encryptednotebook.InitialVector;

public interface InitialVectorProxy {
    byte[] load(String key);

    void save(byte[] iv, String key);
}
