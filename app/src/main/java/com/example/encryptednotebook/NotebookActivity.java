package com.example.encryptednotebook;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

public class NotebookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = findViewById(R.id.text);
                String textValue = text.getText().toString();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("TEXT", textValue).apply();

                Snackbar.make(view, "Udało się zapisać", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
