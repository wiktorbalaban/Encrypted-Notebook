package com.example.encryptednotebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int SET_PASSWORD_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView hello = findViewById(R.id.hello);
        hello.setText("Wpisz hasło");
        boolean isPassCreated = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("PASSWORD_CREATED", false);
        if(!isPassCreated){
            Intent pickContactIntent = new Intent(this, SetPasswordActivity.class);
            startActivityForResult(pickContactIntent, SET_PASSWORD_REQUEST);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText password = findViewById(R.id.password);
                String passValue = password.getText().toString();
                String savedPassValue = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("PASSWORD", null);
                if(passValue.equals(savedPassValue)) {
                    Snackbar.make(view, "Dobre hasło", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(view, "Złe hasło", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
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

        if (requestCode == SET_PASSWORD_REQUEST) {
            if(resultCode == Activity.RESULT_OK){
                Toast.makeText(getApplicationContext(), "Udało się ustawić hasło", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Intent pickContactIntent = new Intent(this, SetPasswordActivity.class);
                startActivityForResult(pickContactIntent, SET_PASSWORD_REQUEST);
            }
        }
    }
}
