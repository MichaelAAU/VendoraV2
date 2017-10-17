package com.aaufolks.android.vendora.Pop_up_screens;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.aaufolks.android.vendora.Model_Classes.Products;
import com.aaufolks.android.vendora.Model_Classes.VMs;
import com.aaufolks.android.vendora.R;

/**
 * Created by michalisgratsias on 10/11/2016.
 */

public class FirstPaymentFragment extends DialogFragment {

    int choice;

    @Override
    public Dialog onCreateDialog (Bundle savedInstance) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_choices,null);

        final CharSequence[] items = new CharSequence[]{"Visa", "PayPal", "MobilePay"};
        choice = 0;

        return new AlertDialog.Builder(getActivity())          // this class provides a fluent interface for constructing
                .setView(v)
                .setTitle(R.string.choose_payment_title)      // an object of Alert Dialog (pop-up)
                .setSingleChoiceItems(items, choice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choice = which;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Products.get(getContext()).setPaymentMethod(choice);
                        Log.d("TAG", "Payment: " + Products.get(getContext()).getPaymentMethod() + " which: "+ which);
                        dialog.dismiss();
                        new VMs(getContext()).getVMs();                 // gets all the VMs that have the chosen product
                    }
                })
                .create();
    }
}
