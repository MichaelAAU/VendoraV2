package com.aaufolks.android.vendora.Controller_Classes;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by michalisgratsias on 13/11/2016.
 */

public class NFCActivity extends ParentActivity {

    private static final String EXTRA_VM_NAME = "com.aaufolks.android.vendora.vm_name";
    private static final String EXTRA_VM_ADDRESS = "com.aaufolks.android.vendora.vm_address";

    public static Intent newIntent(Context packageContext, String vmName, String vmAddress) { // PASSES the vmId as an Intent Extra
        Intent intent = new Intent(packageContext, NFCActivity.class); // for the VMFragment
        intent.putExtra(EXTRA_VM_NAME, vmName);
        intent.putExtra(EXTRA_VM_ADDRESS, vmAddress);
        return intent;
    }

    @Override
    protected Fragment createFragment() {

        String vmName = (String) getIntent().getSerializableExtra(EXTRA_VM_NAME);
        String vmAddress = (String) getIntent().getSerializableExtra(EXTRA_VM_ADDRESS);
        return NFCFragment.newInstance(vmName, vmAddress);
    }
}
