package com.example.encryptednotebook;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import javax.crypto.SecretKey;

import static com.example.encryptednotebook.Cipher.decryptMsg;
import static com.example.encryptednotebook.Cipher.encryptMsg;
import static com.example.encryptednotebook.Cipher.generateKey;

public class NotebookActivity extends AppCompatActivity {

    String mSavedPassValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final EditText text = findViewById(R.id.text);

        mSavedPassValue = getIntent().getStringExtra("DECRYPTED_PASS");
        //= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PASSWORD", null);
        if (mSavedPassValue != null) {
            try {
                SecretKey secretKey = generateKey(mSavedPassValue,getApplicationContext());
                String encryptedText = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("TEXT", null);
                //String decryptedText = decryptMsg(encryptedText.getBytes(),secretKey);
                if (encryptedText != null) {
                    String noteText = decryptMsg(encryptedText, secretKey);
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

                if (mSavedPassValue != null) {
                    try {
                        SecretKey secretKey = generateKey(mSavedPassValue,getApplicationContext());
                        String encryptedText = encryptMsg(textValue, secretKey);
                        String ddddd = decryptMsg(encryptedText, secretKey);
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



}
