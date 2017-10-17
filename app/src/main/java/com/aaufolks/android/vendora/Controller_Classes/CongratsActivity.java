package com.aaufolks.android.vendora.Controller_Classes;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by michalisgratsias on 13/11/2016.
 */

public class CongratsActivity extends ParentActivity {

    private static final String EXTRA_VM_NAME = "com.aaufolks.android.vendora.vm_name";

    public static Intent newIntent(Context packageContext, String vmName) { // PASSES the vmName as an Intent Extra
        Intent intent = new Intent(packageContext, CongratsActivity.class); // for the VMFragment
        intent.putExtra(EXTRA_VM_NAME, vmName);
        return intent;
    }

    @Override
    protected Fragment createFragment() {

        String vmName = (String) getIntent().getSerializableExtra(EXTRA_VM_NAME);
        return CongratsFragment.newInstance(vmName);
    }
}
