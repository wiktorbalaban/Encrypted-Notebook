package com.example.encryptednotebook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.encryptednotebook.CipherFacade.AndroidKeyStoreEncryptionCipherFacade;
import com.example.encryptednotebook.CipherFacade.CipherFacadeException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class NotebookActivity extends AppCompatActivity {

    EditText mText;
    SharedPreferences mPrefs;
    AndroidKeyStoreEncryptionCipherFacade mCipherFacade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mText = findViewById(R.id.text);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCipherFacade = new AndroidKeyStoreEncryptionCipherFacade(mPrefs);

        decryptNote();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(mFabOnClickListener);
    }

    View.OnClickListener mFabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String noteToSave = mText.getText().toString();
            try {
                String encryptedText = mCipherFacade.encrypt(noteToSave, SharedConstants.NOTE);
                mPrefs.edit().putString(SharedConstants.NOTE, encryptedText).apply();

                Snackbar.make(view, getString(R.string.saved_successfully), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (CipherFacadeException e) {
                e.printStackTrace();
                Snackbar.make(view, getString(R.string.saved_failed), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    };

    void decryptNote() {
        try {
            String encryptedText = mPrefs.getString(SharedConstants.NOTE, null);
            if (encryptedText != null) {
                String noteText = mCipherFacade.decrypt(encryptedText, SharedConstants.NOTE);
                mText.setText(noteText);
            } else {
                mText.setText(R.string.your_note);
            }
        } catch (CipherFacadeException e) {
            e.printStackTrace();
        }
    }

}
