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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class MainActivity extends AppCompatActivity {

    private static final int SET_PASSWORD_REQUEST = 1;
    private static final int CHANGE_PASSWORD_REQUEST = 2;

    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String saltString = prefs.getString(SharedConstants.SALT, null);
        if (saltString == null) {
            int saltBytes = 8;
            byte[] salt = new byte[saltBytes];
            int ivBytes = 16;
            byte[] iv = new byte[ivBytes];
            int bigIntegerBits = (saltBytes + ivBytes) * 8;
            String randomString = new BigInteger(bigIntegerBits, new SecureRandom()).toString(32);//TODO: może da się bardzoej losowo
            try {
                byte[] randomStringBytes = randomString.getBytes(StandardCharsets.UTF_8);//To i tak daje więcej bitów niż potrzebne 192
                System.arraycopy(randomStringBytes, 0, salt, 0, 8);
                System.arraycopy(randomStringBytes, 8, iv, 0, 16);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SharedConstants.SALT, new String(salt, StandardCharsets.UTF_8));
                editor.putString(SharedConstants.INITIAL_VECTOR, new String(iv, StandardCharsets.UTF_8));
                editor.apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        boolean isPassCreated = prefs.getBoolean(SharedConstants.PASSWORD_CREATED, false);
        if (!isPassCreated) {
            Intent pickContactIntent = new Intent(this, SetPasswordActivity.class);
            startActivityForResult(pickContactIntent, SET_PASSWORD_REQUEST);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    password = findViewById(R.id.passphrase);
                    String passValue = password.getText().toString();
                    String savedPassValue = prefs.getString(SharedConstants.PASSWORD, null);
                    Cipher cipher = new Cipher(
                            prefs.getString(SharedConstants.SALT, null),
                            prefs.getString(SharedConstants.INITIAL_VECTOR, null),
                            passValue);
                    String savedPassValueDecrypted = cipher.decryptString(savedPassValue);
                    if (passValue.equals(savedPassValueDecrypted)) {
                        Snackbar.make(view, "Dobre hasło", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Intent intent = new Intent(getApplicationContext(), NotebookActivity.class);
                        intent.putExtra(IntentConstants.DECRYPTED_PASS, passValue);
                        startActivity(intent);
                    } else {
                        Snackbar.make(view, "Złe hasło", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(view, "Złe hasło", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        Button changePasswordButton = findViewById(R.id.changePassphrase);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickContactIntent = new Intent(getApplicationContext(), ChangePassword.class);
                startActivityForResult(pickContactIntent, CHANGE_PASSWORD_REQUEST);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (password != null)
            password.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SET_PASSWORD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Udało się ustawić hasło", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Intent pickContactIntent = new Intent(this, SetPasswordActivity.class);
                startActivityForResult(pickContactIntent, SET_PASSWORD_REQUEST);
            }
        } else if (requestCode == CHANGE_PASSWORD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Udało się zmienić hasło", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Nie udało się zmienić hasła", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
