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

import com.aaufolks.android.vendora.Model_Classes.Products;
import com.aaufolks.android.vendora.R;

/**
 * Created by michalisgratsias on 10/11/2016.
 */

public class ChoosePaymentFragment extends DialogFragment {

    public static final String EXTRA_CHOICE = "com.aaufolks.android.vendora.choice";
    public int choice;

    @Override
    public Dialog onCreateDialog (Bundle savedInstance) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_choices,null);

        final CharSequence[] items = new CharSequence[]{"Visa", "PayPal", "MobilePay"};
        choice = Products.get(getContext()).getPaymentMethod();

        return new AlertDialog.Builder(getActivity())          // this class provides a fluent interface for constructing
                .setView(v)
                .setTitle(R.string.choose_payment_title)      // an object of Alert Dialog (pop-up)
                .setSingleChoiceItems(items, choice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choice = which;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {    // here you pass the object that implements
                    @Override                                                                      // the listener interface
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, choice);
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, int choice) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CHOICE, choice);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
