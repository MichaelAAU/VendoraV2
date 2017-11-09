package com.aaufolks.android.vendora.Model_Classes;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.aaufolks.android.vendora.Controller_Classes.ProductActivity;
import com.aaufolks.android.vendora.Controller_Classes.VMActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by michalisgratsias on 3/11/16.
 * Rewritten by michalisgratsias on 16/10/17.
 */

public class VMs implements java.io.Serializable {

    private ArrayList<VendingMachine> mVMs = new ArrayList<VendingMachine>();
    private ArrayList<String> mVMAvailable = new ArrayList<String>();
    private ArrayList<String> mVMReserved = new ArrayList<String>();

    public VMs(final Context context) {
        mVMAvailable.clear();
        mVMReserved.clear();
        mVMs.clear();
        final int chosen = Products.get(context).getChosenProduct();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        DatabaseReference m1Ref = myRef.child("Products & Status");
        m1Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            // Retrieve all available VM products as they are in the database
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    String vmID = (String) child.getKey();
                    Log.d("Tag", "vmId: " + vmID);
                    for (DataSnapshot grandchild: child.getChildren()) {
                        Log.d("Tag", "prod: " + grandchild.getKey());
                        String prodCat = (String) grandchild.child("productCategory").getValue();
                        String prodStat = (String) grandchild.child("productStatus").getValue();
                        String prodCust = (String) grandchild.child("customerID").getValue();
                        if (prodCat.equals(String.valueOf(chosen)) && !mVMAvailable.contains(vmID)
                                && prodStat.equals("Available")) mVMAvailable.add(vmID);
                        if ((prodStat.equals("Reserved") && prodCust.equals(Settings.Secure.getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID)))) mVMReserved.add(vmID);
                    }
                }
            }
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        DatabaseReference m2Ref = myRef.child("Vending Machines");
        m2Ref.addListenerForSingleValueEvent(new ValueEventListener()  {
            // Retrieve available vending machines
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    String vmID = (String) child.getKey();
                    String vmName = (String) child.child("vmName").getValue();
                    String vmAdd = (String) child.child("vmAddress").getValue();
                    String vmIPAdd = (String) child.child("vmIPAddress").getValue();
                    double vmLat = (double) child.child("vmLatitude").getValue();
                    double vmLong = (double) child.child("vmLongitude").getValue();
                    VendingMachine vendingMachine = new VendingMachine();
                    if (mVMAvailable.contains(vmID) || mVMReserved.contains(vmID)) {
                        vendingMachine.setVMId(vmID);
                        Log.d("Tag", "Id: " + vendingMachine.getVMId());
                        vendingMachine.setVMName(vmName);
                        Log.d("Tag", "Name: " + vendingMachine.getVMName());
                        vendingMachine.setVMAddress(vmAdd);
                        Log.d("Tag", "Address: " + vendingMachine.getVMAddress());
                        vendingMachine.setVMLat(vmLat);
                        Log.d("Tag", "Latitude: " + vendingMachine.getVMLat());
                        vendingMachine.setVMLon(vmLong);
                        Log.d("Tag", "Longitude: " + vendingMachine.getVMLon());
                        vendingMachine.setVMIP(vmIPAdd);
                        Log.d("Tag", "Address: " + vendingMachine.getVMIP());
                        vendingMachine.setProdAvailable(false);
                        if (mVMAvailable.contains(vmID)) vendingMachine.setProdAvailable(true);
                        Log.d("Tag", "Available: " + vendingMachine.isProdAvailable());
                        mVMs.add(vendingMachine);
                    }
                }
                Intent intent = VMActivity.newIntent(context, mVMs);
                context.startActivity(intent);
            }
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    public VendingMachine getVendingMachine(String id) {    // get a Vending Machine by ID
        for (VendingMachine vm : mVMs) {
            if (vm.getVMId() == id)
                return vm;
        }
        return null;
    }

    public ArrayList<VendingMachine> getVMs() {          // get all Vending Machines
        return mVMs;
    }

}
