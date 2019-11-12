package com.example.encryptednotebook;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class NotebookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final EditText text = findViewById(R.id.text);

        String savedPassValue = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PASSWORD", null);
        if (savedPassValue != null) {
            try {
                SecretKey secretKey = generateKey(savedPassValue);
                String encryptedText = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("TEXT", null);
                if (encryptedText != null) {
                    String noteText = decryptMsg(encryptedText.getBytes(), secretKey);
                    text.setText(noteText);
                } else {
                    text.setText("Twoja notatka");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textValue = text.getText().toString();

                String savedPassValue = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PASSWORD", null);
                if (savedPassValue != null) {
                    try {
                        SecretKey secretKey = generateKey(savedPassValue);
                        String encryptedText = new String(encryptMsg(textValue, secretKey));
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("TEXT", encryptedText).apply();

                        Snackbar.make(view, "Udało się zapisać", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public SecretKey generateKey(String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        //new PBEKeySpec(password, salt, 10, 128)
        String saltString = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("SALT", null);
        byte[] saltBytes;
        try {
            saltBytes = saltString.getBytes();
        } catch (NullPointerException e) {
            e.printStackTrace();
            saltBytes = "".getBytes();
        }
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), saltBytes, 10, 128);
        return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(keySpec).getEncoded(), "AES");
    }

    public byte[] encryptMsg(String message, SecretKey secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return cipherText;
    }

    public String decryptMsg(byte[] cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        return decryptString;
    }

}
