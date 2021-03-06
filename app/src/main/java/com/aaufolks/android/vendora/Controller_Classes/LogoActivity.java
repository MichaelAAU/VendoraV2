package com.aaufolks.android.vendora.Controller_Classes;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.aaufolks.android.vendora.Model_Classes.Products;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.List;

import com.aaufolks.android.vendora.R;
import com.google.firebase.auth.FirebaseUser;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by michalisgratsias on 08/11/2016.
 * Rewritten by michalisgratsias on 09/11/2017.
 */

public class LogoActivity extends AppCompatActivity {

    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final String GOOGLE_PRIVACY_POLICY_URL = "https://www.google.com/policies/privacy/";
    private static final int RC_SIGN_IN = 100; // Request code for sign-in
    static boolean active = false;

    String password = "db760abd8f6403fa";
    String message = password + "done";
    SecretKey secret = generateKey();
    byte[] encrMessage = encryptMsg(message, secret);
    String decrMessage = decryptMsg(encrMessage, secret);

    public LogoActivity() throws NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidParameterSpecException, InvalidKeySpecException {
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, LogoActivity.class);
    }

    @Override
    public void onBackPressed() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) { //Cancel User if exists
            AuthUI.getInstance()
                    .delete(this) // user deleted
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) { // smart pw lock deletes credentials
                            if (task.isSuccessful()) { //deletion succeded
                            } else {
                                showSnackbar(R.string.delete_account_failed);
                            }
                        }
                    });
        }
        active = false;
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_logo);
        Products.get(getApplicationContext()).getProducts();
        active = true;

        // 1. Check if user already signed in, otherwise continue
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startSignedInActivity(null);
            finish();
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MediaPlayer mp = MediaPlayer.create(LogoActivity.this, R.raw.intro);
        mp.start(); // play sound
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (active) {
                    //mp.stop();
                    //if (mp != null) mp.release();
                    startActivityForResult( // Create Authentication instance on default app and sign-in by building a sign-in intent
                            AuthUI.getInstance().createSignInIntentBuilder()
                                    .setLogo(R.drawable.vendora_small_icon)
                                    .setAvailableProviders(getSelectedProviders())
                                    .setTosUrl(GOOGLE_TOS_URL)
                                    .setPrivacyPolicyUrl(GOOGLE_PRIVACY_POLICY_URL)
                                    .setIsSmartLockEnabled(true, true)
                                    .setAllowNewEmailAccounts(true)
                                    .build(), RC_SIGN_IN); // RC = Request code for sign-in
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) { handleSignInResponse(resultCode, data); return;}
        showSnackbar(R.string.unknown_response);
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {    // extracting the ID token from the response of the result intent
        final IdpResponse response = IdpResponse.fromResultIntent(data);      // that the Identity Provider (idp) returned
        if (resultCode == RESULT_OK) {                                  // Successfully signed in
            //Products.get(getApplicationContext()).getProducts();
            MediaPlayer mp2 = MediaPlayer.create(getApplicationContext(), R.raw.success);
            mp2.start();                                                // play sound
            showSnackbar(R.string.successful_sign_in);
            startSignedInActivity(response);
            finish();
            return;
        } else {                                                        // Sign in failed
            if (response == null) {
                showSnackbar(R.string.sign_in_cancelled);
                return;}                                                // User pressed back button
            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;}
            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;}
        }
        showSnackbar(R.string.unknown_sign_in_response);
    }

    @MainThread
    private List<IdpConfig> getSelectedProviders() {
        List<IdpConfig> selectedProviders = new ArrayList<>();
        selectedProviders.add(new IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        selectedProviders.add(new IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        selectedProviders.add(new IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());
        return selectedProviders;
    }

    private void startSignedInActivity(IdpResponse response) {
        Intent intent = ProductActivity.newIntent(getApplicationContext(), response);
        getApplicationContext().startActivity(intent);
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        View mRootView = findViewById(R.id.logo_screen);
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    public SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new SecretKeySpec(password.getBytes(), "AES");
    }

    public static byte[] encryptMsg(String message, SecretKey secret) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        Log.d("Tag", "Ciphertext: " + cipherText);
        return cipherText;
    }

    public static String decryptMsg(byte[] cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException
    {
    /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        Log.d("Tag", "Plaintext: " + decryptString);
        return decryptString;
    }
}
