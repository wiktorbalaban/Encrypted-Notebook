package com.example.encryptednotebook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

public class ChangePassword extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText setPass1 = findViewById(R.id.setPass1);
                String setPass1Value = setPass1.getText().toString();
                EditText setPass2 = findViewById(R.id.setPass2);
                String setPass2Value = setPass2.getText().toString();
                if (setPass1Value.equals(setPass2Value)) {
                    try {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        try {
                            EditText password = findViewById(R.id.oldPass);
                            String oldPasswordUserInput = password.getText().toString();
                            String savedPassValue = prefs.getString(SharedConstants.PASSWORD, null);
                            String cipherSalt = prefs.getString(SharedConstants.SALT, null);
                            String cipherInitialVector = prefs.getString(SharedConstants.INITIAL_VECTOR, null);
                            CipherOld oldCipher = new CipherOld(cipherInitialVector);
                            String savedPassValueDecrypted = oldCipher.decryptString(savedPassValue,prefs);
                            if (oldPasswordUserInput.equals(savedPassValueDecrypted)) {
                                CipherOld newCipher = new CipherOld(cipherInitialVector);
                                String encryptedPassword = newCipher.encryptString(setPass2Value,prefs);

                                String encryptedOldText = prefs.getString(SharedConstants.NOTE, null);
                                if (encryptedOldText != null) {
                                    String decryptedText = oldCipher.decryptString(encryptedOldText,prefs);
                                    String encryptedNewText = newCipher.encryptString(decryptedText,prefs);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(SharedConstants.PASSWORD, encryptedPassword);
                                    editor.putString(SharedConstants.NOTE, encryptedNewText);
                                    editor.apply();
                                    Intent resultIntent = new Intent();
                                    setResult(Activity.RESULT_OK, resultIntent);
                                    finish();
                                }

                            } else {
                                Snackbar.make(view, R.string.wrong_passphrase, Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Snackbar.make(view, R.string.wrong_passphrase, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(view, R.string.different_passphrases_message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

}
