package com.example.encryptednotebook.InitialVector;

import android.content.SharedPreferences;

import java.util.Base64;

public class SharedPreferencesInitialVector implements InitialVectorProxy {

    private final static String keyBegin = "IV_";

    private SharedPreferences prefs;

    public SharedPreferencesInitialVector(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @Override
    public byte[] load(String key) {
        String ivString = prefs.getString(keyBegin + key, "");
        return Base64.getDecoder().decode(ivString);
    }

    @Override
    public void save(byte[] iv, String key) {
        String ivString = Base64.getEncoder().encodeToString(iv);
        prefs.edit().putString(keyBegin + key, ivString).apply();
    }
}
