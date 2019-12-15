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

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class SetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            CipherOld.generateKey();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

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
                        CipherOld cipher = new CipherOld(
                                prefs.getString(SharedConstants.INITIAL_VECTOR, null));
                        String encryptedText = cipher.encryptString(setPass2Value,prefs);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(SharedConstants.PASSWORD_CREATED, true);
                        editor.putString(SharedConstants.PASSWORD, encryptedText);
                        editor.apply();

                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(view, getString(R.string.different_passphrases_message), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
}
