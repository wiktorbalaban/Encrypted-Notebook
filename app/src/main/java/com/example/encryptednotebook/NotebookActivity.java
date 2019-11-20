package com.example.encryptednotebook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class NotebookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final EditText text = findViewById(R.id.text);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String password = getIntent().getStringExtra(IntentConstants.DECRYPTED_PASS);
        if (password != null) {
            try {
                Cipher cipher = new Cipher(
                        prefs.getString(SharedConstants.SALT, null),
                        prefs.getString(SharedConstants.INITIAL_VECTOR, null),
                        password);
                String encryptedText = prefs.getString(SharedConstants.NOTE, null);
                if (encryptedText != null) {
                    String noteText = cipher.decryptString(encryptedText);
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
                String noteToSave = text.getText().toString();

                if (password != null) {
                    try {
                        Cipher cipher = new Cipher(
                                prefs.getString(SharedConstants.SALT, null),
                                prefs.getString(SharedConstants.INITIAL_VECTOR, null),
                                password);
                        String encryptedText = cipher.encryptString(noteToSave);
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString(SharedConstants.NOTE, encryptedText).apply();

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
