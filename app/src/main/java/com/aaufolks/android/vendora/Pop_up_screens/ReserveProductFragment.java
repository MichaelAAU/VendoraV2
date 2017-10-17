package com.aaufolks.android.vendora.Pop_up_screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aaufolks.android.vendora.R;


/**
 * Created by michalisgratsias on 10/11/2016.
 */

public class ReserveProductFragment extends DialogFragment {

    public static final String EXTRA_MACHINE_NAME = "com.aaufolks.android.vendora.machineName";
    private static final String ARG_PRODUCT = "product";
    private static final String ARG_VM_NAME = "vendingMachineName";
    private TextView mQuestion;

    public static ReserveProductFragment newInstance(String product, String vendingMachineName) {  // method to set fragment arguments
        Bundle args = new Bundle();                                   // that replaces the usual fragment constructor
        args.putSerializable(ARG_PRODUCT, product);
        args.putSerializable(ARG_VM_NAME, vendingMachineName);
        ReserveProductFragment fragment = new ReserveProductFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstance) {

        final String productName = (String) getArguments().getSerializable(ARG_PRODUCT);
        final String vmName = (String) getArguments().getSerializable(ARG_VM_NAME);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_reserve,null);

        mQuestion = (TextView) v.findViewById(R.id.reserve);
        mQuestion.setText("Are you sure that you want to reserve a " + productName
                + " at " + vmName + " vending Machine?");

        return new AlertDialog.Builder(getActivity())          // this class provides a fluent interface for constructing
                .setView(v)
                .setTitle(R.string.make_reservation_title)      // an object of Alert Dialog (pop-up)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {    // here you pass the object that implements
                    @Override                                                                      // the listener interface
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, vmName);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {@Override public void onClick(DialogInterface dialog, int which) {sendResult(Activity.RESULT_CANCELED, vmName);}})
                .create();
    }

    private void sendResult(int resultCode, String machineName) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_MACHINE_NAME, machineName);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
