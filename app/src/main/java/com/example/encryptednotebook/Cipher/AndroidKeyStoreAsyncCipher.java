package com.example.encryptednotebook.Cipher;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import com.example.encryptednotebook.R;
import com.example.encryptednotebook.SecretKey.SecretKeyException;
import com.example.encryptednotebook.SecretKey.SecretKeyProvider;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.Executor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AndroidKeyStoreAsyncCipher implements AsyncCipher {

    private static final String CIPHER_TYPE = "AES/GCM/NoPadding";
    private static final String SECRET = "AndroidKeyStoreCipherSecret";

    private byte[] iv;
    private SecretKeyProvider secretKeyProvider;
    BiometricPrompt.PromptInfo promptInfo;
    FragmentActivity activity;
    DecryptFinishEventListener decryptFinishEventListener;
    EncryptFinishEventListener encryptFinishEventListener;
    String encryptedMessage;
    String decryptedMessage;

    public AndroidKeyStoreAsyncCipher(SecretKeyProvider secretKeyProvider, FragmentActivity activity) {
        this.secretKeyProvider = secretKeyProvider;
        this.activity = activity;
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.auth)) // required
                .setSubtitle(activity.getString(R.string.authorize_yourself))
                .setDescription(activity.getString(R.string.explain_auth))
                .setNegativeButtonText(activity.getString(R.string.cancel)) // required
                .build();
    }

    @Override
    public void encrypt(String message) throws CipherException {
        try {
            decryptedMessage = message;
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            SecretKey secretKey = secretKeyProvider.get(SECRET);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            iv = cipher.getIV();
            BiometricPrompt.CryptoObject promptCrypto = new BiometricPrompt.CryptoObject(cipher);
            BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, encryptCallback);
            biometricPrompt.authenticate(promptInfo, promptCrypto);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | SecretKeyException | InvalidKeyException e) {
            e.printStackTrace();
            throw new CipherException();
        }
    }

    @Override
    public void decrypt(String encryptedMessage) throws CipherException {
        try {
            this.encryptedMessage = encryptedMessage;
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            GCMParameterSpec ivSpec =
                    new GCMParameterSpec(128, iv);//TODO: co to tLen???
            cipher.init(Cipher.DECRYPT_MODE, secretKeyProvider.get(SECRET), ivSpec);
            BiometricPrompt.CryptoObject promptCrypto = new BiometricPrompt.CryptoObject(cipher);
            BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, decryptCallback);
            biometricPrompt.authenticate(promptInfo, promptCrypto);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | SecretKeyException
                | InvalidKeyException //| BadPaddingException | IllegalBlockSizeException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            throw new CipherException();
        }
    }

    @Override
    public void setDecryptFinishEventListener(DecryptFinishEventListener decryptFinishEventListener) {
        this.decryptFinishEventListener = decryptFinishEventListener;
    }

    @Override
    public void setEncryptFinishEventListener(EncryptFinishEventListener encryptFinishEventListener) {
        this.encryptFinishEventListener = encryptFinishEventListener;
    }

    public Executor executor = new Executor() {
        @Override
        public void execute(@NonNull Runnable runnable) {
            runnable.run();
        }
    };

    public BiometricPrompt.AuthenticationCallback decryptCallback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            try {
                Cipher cipher = result.getCryptoObject().getCipher();
                byte[] messageBytes = Base64.getDecoder().decode(encryptedMessage);
                byte[] decryptedBytes = cipher.doFinal(messageBytes);
                String decryptedMessage = new String(decryptedBytes, StandardCharsets.UTF_8);
                if (decryptFinishEventListener != null)
                    decryptFinishEventListener.onDecryptFinishEvent(decryptedMessage);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                if (decryptFinishEventListener != null)
                    decryptFinishEventListener.onDecryptFinishEvent("");
            }
        }
    };

    public BiometricPrompt.AuthenticationCallback encryptCallback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            try {
                Cipher cipher = result.getCryptoObject().getCipher();
                byte[] iv = cipher.getIV();
                byte[] utfMessageBytes = decryptedMessage.getBytes(StandardCharsets.UTF_8);
                byte[] cipherText = cipher.doFinal(utfMessageBytes);
                String encryptedMesage = Base64.getEncoder().encodeToString(cipherText);
                if (encryptFinishEventListener != null)
                    encryptFinishEventListener.onEncryptFinishEvent(encryptedMesage, iv);
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                if (encryptFinishEventListener != null)
                    encryptFinishEventListener.onEncryptFinishEvent("", new byte[1]);//TODO: on failed event
            }
        }
    };

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
}
