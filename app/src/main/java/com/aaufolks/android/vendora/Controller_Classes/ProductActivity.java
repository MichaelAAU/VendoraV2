package com.aaufolks.android.vendora.Controller_Classes;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.aaufolks.android.vendora.Model_Classes.Products;

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
