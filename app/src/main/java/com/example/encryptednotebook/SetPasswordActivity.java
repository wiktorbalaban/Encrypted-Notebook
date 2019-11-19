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

import javax.crypto.SecretKey;

import static com.example.encryptednotebook.Cipher.encryptMsg;
import static com.example.encryptednotebook.Cipher.generateKey;

public class SetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
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
                        SecretKey secretKey = generateKey(setPass2Value, getApplicationContext());
                        String encryptedText = encryptMsg(setPass2Value, secretKey);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        prefs.edit().putBoolean("PASSWORD_CREATED", true).apply();
                        prefs.edit().putString("PASSWORD", encryptedText).apply();
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(view, "Hasła się różnią, wpisz takie same hasła", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

}