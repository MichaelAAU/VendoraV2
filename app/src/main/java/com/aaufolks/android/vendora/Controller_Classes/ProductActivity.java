package com.aaufolks.android.vendora.Controller_Classes;

import android.support.v4.app.Fragment;

/**
 * Created by michalisgratsias on 26/10/16.
 */

public class ProductActivity extends ParentActivity {

    @Override
    public Fragment createFragment() {
        return new ProductFragment();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
