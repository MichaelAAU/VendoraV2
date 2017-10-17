package com.aaufolks.android.vendora.Controller_Classes;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.aaufolks.android.vendora.Model_Classes.VendingMachine;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;

/**
 * Created by michalisgratsias on 2/11/16.
 */

public class VMActivity extends ParentActivity {

    private static final int REQUEST_ERROR = 0;
    private static final String EXTRA_VMs = "com.aaufolks.android.vendora.VMs";

    public static Intent newIntent(Context packageContext, ArrayList<VendingMachine> vms) { // PASSES the vms as an Intent Extra
        Intent intent = new Intent(packageContext, VMActivity.class);                       // for the VMFragment
        intent.putExtra(EXTRA_VMs, vms);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        ArrayList<VendingMachine> vms = (ArrayList<VendingMachine>) getIntent().getSerializableExtra(EXTRA_VMs);
        return VMFragment.newInstance(vms);
    }

    @Override
    protected void onResume() {         // This is to verify that Play Services are available on the device
        super.onResume();

        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GooglePlayServicesUtil
                    .getErrorDialog(errorCode, this, REQUEST_ERROR,
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                        // Leave if services are unavailable.
                                    Log.d("Tag", "Service Unavailable.");
                                    finish();
                                }
                            });

            errorDialog.show();
        }
    }
}
