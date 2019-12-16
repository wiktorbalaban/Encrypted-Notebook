package com.example.encryptednotebook.Cipher;

import com.example.encryptednotebook.SecretKey.SecretKeyException;
import com.example.encryptednotebook.SecretKey.SecretKeyProvider;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;

public class AndroidKeyStoreCipher implements Cipher {

    private static final String CIPHER_TYPE = "AES/GCM/NoPadding";
    private static final String SECRET = "AndroidKeyStoreCipherSecret";

    private byte[] iv;
    private SecretKeyProvider secretKeyProvider;

    public AndroidKeyStoreCipher(SecretKeyProvider secretKeyProvider) {
        this.secretKeyProvider = secretKeyProvider;
    }

    @Override
    public String encrypt(String message) throws CipherException {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeyProvider.get(SECRET));
            iv = cipher.getIV();
            //prefs.edit().putString(SharedConstants.INITIAL_VECTOR, Base64.getEncoder().encodeToString(iv)).apply();//TODO: trzeba zapisywać iv osovbno dla pass i notatki

            byte[] utfMessageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] cipherText = cipher.doFinal(utfMessageBytes);
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | SecretKeyException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new CipherException();
        }
    }

    @Override
    public String decrypt(String encryptedMessage) throws CipherException {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);
            //String iv = prefs.getString(SharedConstants.INITIAL_VECTOR, null);
            GCMParameterSpec ivSpec =
                    new GCMParameterSpec(128, Base64.getDecoder().decode(iv));//TODO: co to tLen???
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKeyProvider.get(SECRET), ivSpec);
            byte[] messageBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(messageBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | SecretKeyException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            throw new CipherException();
        }
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
}
