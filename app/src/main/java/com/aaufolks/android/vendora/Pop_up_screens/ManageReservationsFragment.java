package com.aaufolks.android.vendora.Pop_up_screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.aaufolks.android.vendora.Model_Classes.MyReservations;
import com.aaufolks.android.vendora.Model_Classes.Products;

import com.aaufolks.android.vendora.R;

import java.util.ArrayList;

/**
 * Created by michalisgratsias on 12/11/2016.
 */

public class ManageReservationsFragment extends DialogFragment {

    public static final String EXTRA_CHOICE = "com.aaufolks.android.vendora.reservations";
    int choice;

    @Override
    public Dialog onCreateDialog (Bundle savedInstance) {

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_choices,null);

        final int reservationAmount = MyReservations.get().getMyReservations().size();
        //final ArrayList mSelectedItems = new ArrayList();  // Where we track the selected items
        final CharSequence[] items = new CharSequence[reservationAmount];
        //final boolean[] checkedItems = new boolean[reservationAmount];

        choice = -1;
        for (int i = 0; i< reservationAmount; i++) {
            items[i] = Products.get(getContext()).getProduct(MyReservations.get().getMyReservations().get(i).getProductId()).getProductName()
                    + " at " + MyReservations.get().getMyReservations().get(i).getVMName();
        }

        return new AlertDialog.Builder(getActivity())          // this class provides a fluent interface for constructing
                .setView(v)
                .setTitle(R.string.manage_reservations_title)      // an object of Alert Dialog (pop-up)
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
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
                {@Override public void onClick(DialogInterface dialog, int which) {sendResult(Activity.RESULT_CANCELED, choice);}})
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
