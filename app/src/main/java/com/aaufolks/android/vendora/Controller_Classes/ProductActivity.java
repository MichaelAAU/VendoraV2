package com.aaufolks.android.vendora.Controller_Classes;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;

import com.aaufolks.android.vendora.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by michalisgratsias on 26/10/16.
 * Rewritten by michalisgratsias on 9/11/17.
 */

public class ProductActivity extends ParentActivity {

    private static final String EXTRA_ID_RESPONSE = "extra_id_response";

    public static Intent newIntent(Context packageContext, IdpResponse response) { // PASSES the response as an Intent Extra
        Intent intent = new Intent(packageContext, ProductActivity.class);         // for the Product Fragment
        intent.putExtra(EXTRA_ID_RESPONSE, response);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        IdpResponse response = (IdpResponse) getIntent().getParcelableExtra(EXTRA_ID_RESPONSE);
        return ProductFragment.newInstance(response);
    }

    @Override
    public void onBackPressed() {
        AuthUI.getInstance()
                .signOut(this) //sign out of Auth instance
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {// smart lock disabled, sign out of identity provider
                            startActivity(LogoActivity.createIntent(ProductActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                            finishAffinity();
                        }
                    }
                });
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        View mProductView = findViewById(R.id.activity_product);
        Snackbar.make(mProductView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
}
