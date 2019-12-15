package com.example.encryptednotebook.SecretKey;

import javax.crypto.SecretKey;

public interface SecretKeyProvider {
    SecretKey get(String keyAlias) throws SecretKeyException;
}
