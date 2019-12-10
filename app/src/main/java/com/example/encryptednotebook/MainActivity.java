package com.example.encryptednotebook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.an.biometric.BiometricCallback;
import com.an.biometric.BiometricManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Enumeration;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

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
            int ivBytes = 12;
            byte[] iv = new byte[ivBytes];
            int bigIntegerBits = (saltBytes + ivBytes) * 8;
            String randomString = new BigInteger(bigIntegerBits, new SecureRandom()).toString(32);//TODO: może da się bardzoej losowo
            try {
                byte[] randomStringBytes = randomString.getBytes(StandardCharsets.UTF_8);//To i tak daje więcej bitów niż potrzebne 192
                System.arraycopy(randomStringBytes, 0, salt, 0, 8);
                System.arraycopy(randomStringBytes, 8, iv, 0, 12);
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
                boolean showWrongPassphraseMessage = false;
                try {
                    password = findViewById(R.id.passphrase);
                    String passValue = password.getText().toString();
                    String savedPassValue = prefs.getString(SharedConstants.PASSWORD, null);
                    Cipher cipher = new Cipher(
                            prefs.getString(SharedConstants.INITIAL_VECTOR, null));
                    String savedPassValueDecrypted = cipher.decryptString(savedPassValue, prefs);
                    if (passValue.equals(savedPassValueDecrypted)) {
                        Intent intent = new Intent(getApplicationContext(), NotebookActivity.class);
                        intent.putExtra(IntentConstants.DECRYPTED_PASS, passValue);
                        startActivity(intent);
                    } else {
                        showWrongPassphraseMessage = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showWrongPassphraseMessage = true;
                }
                if (showWrongPassphraseMessage) {
                    Snackbar.make(view, getString(R.string.wrong_passphrase), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

//        try {
//            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"); // 1
//            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder("MyKeyAlias",
//                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//                    //.setUserAuthenticationRequired(true) // 2 requires lock screen, invalidated if lock screen is disabled
//                    //.setUserAuthenticationValidityDurationSeconds(120) // 3 only available x seconds from password authentication. -1 requires finger print - every time
//                    .setRandomizedEncryptionRequired(true) // 4 different ciphertext for same plaintext on each call
//                    .build();
//            keyGenerator.init(keyGenParameterSpec);
//            keyGenerator.generateKey();
//
//        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
//            e.printStackTrace();
//        }


//        try {
//            char[] password = "1243".toCharArray();
////            KeyStore.ProtectionParameter protParam =
//////                    new KeyStore.PasswordProtection(password);
//            KeyProtection keyProtection = new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//                    .build();
//            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
//            ks.load(null);
//            Enumeration<String> aliases = ks.aliases();
//            Cipher cipher = new Cipher(
//                    prefs.getString(SharedConstants.SALT, null),
//                    prefs.getString(SharedConstants.INITIAL_VECTOR, null),
//                    "123");
//            SecretKey secretKey = cipher.getSecretKey();
//            KeyStore.SecretKeyEntry skEntry =
//                    new KeyStore.SecretKeyEntry(secretKey);
//            ks.setEntry("secretKeyAlias", skEntry, keyProtection);
//            aliases = ks.aliases();
//            KeyStore.SecretKeyEntry skEntry2 = (KeyStore.SecretKeyEntry)
//                    ks.getEntry("secretKeyAlias", keyProtection);
//            SecretKey secretKey2 = skEntry2.getSecretKey();
//            aliases = ks.aliases();
//        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | InvalidKeySpecException | UnrecoverableEntryException e) {
//            e.printStackTrace();
//        }

//        new BiometricManager.BiometricBuilder(MainActivity.this)
////                .setTitle(getString(R.string.unlock_note))
////                .setSubtitle("")
////                .setDescription("")
////                .setNegativeButtonText(getString(R.string.cancel))
////                .build()
////                .authenticate(biometricCallback);
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
        if (id == R.id.action_change_passphrase) {
            Intent pickContactIntent = new Intent(getApplicationContext(), ChangePassword.class);
            startActivityForResult(pickContactIntent, CHANGE_PASSWORD_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SET_PASSWORD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), R.string.passphrase_set_successfully, Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Intent pickContactIntent = new Intent(this, SetPasswordActivity.class);
                startActivityForResult(pickContactIntent, SET_PASSWORD_REQUEST);
            }
        } else if (requestCode == CHANGE_PASSWORD_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(getApplicationContext(), R.string.passphrase_change_successfully, Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), R.string.passphrase_change_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    BiometricCallback biometricCallback = new BiometricCallback() {
        @Override
        public void onSdkVersionNotSupported() {
            /*
             *  Will be called if the device sdk version does not support Biometric authentication
             */
            Toast.makeText(getApplicationContext(), "the device sdk version does not support Biometric authentication", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBiometricAuthenticationNotSupported() {
            /*
             *  Will be called if the device does not contain any fingerprint sensors
             */
            Toast.makeText(getApplicationContext(), "the device does not contain any fingerprint sensors", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBiometricAuthenticationNotAvailable() {
            /*
             *  The device does not have any biometrics registered in the device.
             */
            Toast.makeText(getApplicationContext(), "The device does not have any biometrics registered in the device.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBiometricAuthenticationPermissionNotGranted() {
            /*
             *  android.permission.USE_BIOMETRIC permission is not granted to the app
             */
            Toast.makeText(getApplicationContext(), "android.permission.USE_BIOMETRIC permission is not granted to the app", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBiometricAuthenticationInternalError(String error) {
            /*
             *  This method is called if one of the fields such as the title, subtitle,
             * description or the negative button text is empty
             */
            Toast.makeText(getApplicationContext(), "This method is called if one of the fields such as the title, subtitle, description or the negative button text is empty", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationFailed() {
            /*
             * When the fingerprint doesn’t match with any of the fingerprints registered on the device,
             * then this callback will be triggered.
             */
            Toast.makeText(getApplicationContext(), "the fingerprint doesn’t match with any of the fingerprints registered on the device", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationCancelled() {
            /*
             * The authentication is cancelled by the user.
             */
            Toast.makeText(getApplicationContext(), "The authentication is cancelled by the user.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationSuccessful() {
            /*
             * When the fingerprint is has been successfully matched with one of the fingerprints
             * registered on the device, then this callback will be triggered.
             */
            Toast.makeText(getApplicationContext(), "the fingerprint has been successfully matched with one of the fingerprints registered on the device", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            /*
             * This method is called when a non-fatal error has occurred during the authentication
             * process. The callback will be provided with an help code to identify the cause of the
             * error, along with a help message.
             */
            Toast.makeText(getApplicationContext(), "a non-fatal error has occurred during the authentication process", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            /*
             * When an unrecoverable error has been encountered and the authentication process has
             * completed without success, then this callback will be triggered. The callback is provided
             * with an error code to identify the cause of the error, along with the error message.
             */
            Toast.makeText(getApplicationContext(), "an unrecoverable error has been encountered: " + errString, Toast.LENGTH_SHORT).show();
        }
    };
}
