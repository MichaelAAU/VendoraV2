package com.aaufolks.android.vendora.Controller_Classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aaufolks.android.vendora.Model_Classes.MyReservations;
import com.aaufolks.android.vendora.Model_Classes.Products;
import com.aaufolks.android.vendora.Pop_up_screens.ChoosePaymentFragment;
import com.aaufolks.android.vendora.Pop_up_screens.ManageReservationsFragment;
import com.aaufolks.android.vendora.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.aaufolks.android.vendora.R.drawable.pay_with_mobile_pay;
import static com.aaufolks.android.vendora.R.drawable.pay_with_paypal;
import static com.aaufolks.android.vendora.R.drawable.pay_with_visa;
import static com.aaufolks.android.vendora.R.drawable.reservations_0;
import static com.aaufolks.android.vendora.R.drawable.reservations_1;
import static com.aaufolks.android.vendora.R.drawable.reservations_2;
import static com.aaufolks.android.vendora.R.drawable.reservations_3;
import static com.aaufolks.android.vendora.R.drawable.reservations_4;
import static com.aaufolks.android.vendora.R.drawable.reservations_5;

/**
 * Created by michalisgratsias on 13/11/2016.
 */

public class CongratsFragment extends Fragment {

    private static final String ARG_VM_NAME = "vm_name";
    private TextView mTextCongrats;
    private Button mButtonStartAgain;
    private String vmName;
    private String mProduct;
    private boolean cancelled;
    private ProgressDialog progressCircle;

    public static CongratsFragment newInstance(String vmName) {   // we use a method to create Fragment instead of using Constructor
        Bundle args = new Bundle();                         // creates Bundle for arguments
        args.putSerializable(ARG_VM_NAME, vmName);          // adds vmName to Bundle
        CongratsFragment fragment = new CongratsFragment(); // creates Fragment instance
        fragment.setArguments(args);                        // sets Arguments
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {   // it is Public because it can be called by various activities hosting it

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        vmName = (String) getArguments().getSerializable(ARG_VM_NAME);   // accessing Fragment arguments for vmName
        ((CongratsActivity)getActivity()).getSupportActionBar().setTitle("Deliver Products");
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_congrats, parent, false);
        mTextCongrats = (TextView) view.findViewById(R.id.text_product);
        mButtonStartAgain = (Button) view.findViewById(R.id.button_start_again);

        String text = "Your order:\n"; int price = 0;
        int reservations = MyReservations.get().getMyReservations().size();
        Log.d("Vendora", "Reservations= " + reservations);
        for (int i = 0; i< reservations; i++) {  // collects products to be delivered
            Log.d("Vendora", "i= " + i);
            Log.d("Vendora", "My VM Name= "+MyReservations.get().getMyReservations().get(i).getVMName());
            Log.d("Vendora", "The sent VM Name= "+vmName);
            if (MyReservations.get().getMyReservations().get(i).getVMName().equals(vmName)) {   // reservations on this machine
                mProduct = Products.get(getContext()).getProduct(MyReservations.get().getMyReservations().get(i).getProductId()).getProductName();
                Log.d("Vendora", "My product= "+mProduct);
                text = text + mProduct + "\n";
                price = price + Products.get(getContext()).getProduct(MyReservations.get().getMyReservations().get(i).getProductId()).getProductPrice();
            }
        }
        text = text + "\nYou are paying: " + price + " kr.";
        Log.d("Vendora", "Text= " + text);
        mTextCongrats.setText(text);
        for (int i = 0; i< reservations; i++) {  // removes reservations
            for (int j = 0; j< MyReservations.get().getMyReservations().size(); j++) {
                if (MyReservations.get().getMyReservations().get(j).getVMName().equals(vmName)) {
                    MyReservations.get().getMyReservations().remove(j);
                    break;
                }
            }
        }

        mButtonStartAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProductActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        switch (MyReservations.get().getMyReservations().size()) {
            case 0: {
                menu.findItem(R.id.manage_reservations).setIcon(reservations_0);
                break;
            }
            case 1: {
                menu.findItem(R.id.manage_reservations).setIcon(reservations_1);
                break;
            }
            case 2: {
                menu.findItem(R.id.manage_reservations).setIcon(reservations_2);
                break;
            }
            case 3: {
                menu.findItem(R.id.manage_reservations).setIcon(reservations_3);
                break;
            }
            case 4: {
                menu.findItem(R.id.manage_reservations).setIcon(reservations_4);
                break;
            }
            case 5: {
                menu.findItem(R.id.manage_reservations).setIcon(reservations_5);
                break;
            }
        }
        switch (Products.get(getContext()).getPaymentMethod()) {
            case 0: {
                menu.findItem(R.id.choose_payment).setIcon(pay_with_visa);
                break;
            }
            case 1: {
                menu.findItem(R.id.choose_payment).setIcon(pay_with_paypal);
                break;
            }
            case 2: {
                menu.findItem(R.id.choose_payment).setIcon(pay_with_mobile_pay);
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manage_reservations:   //shows dialog for managing reservations
                if (MyReservations.get().getMyReservations().size()>0) {
                    ManageReservationsFragment dialog1 = new ManageReservationsFragment();
                    FragmentManager manager1 = getFragmentManager();
                    dialog1.setTargetFragment(CongratsFragment.this, 3);
                    dialog1.show(manager1, "DialogManageReservations");
                } else {
                    Toast.makeText(getContext(), "There are no Reservations to show!", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.choose_payment:   //shows dialog for choosing payment
                ChoosePaymentFragment dialog2 = new ChoosePaymentFragment();
                FragmentManager manager2 = getFragmentManager();
                dialog2.setTargetFragment(CongratsFragment.this, 2);
                dialog2.show(manager2, "DialogChoosePayment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 2: {       // SETTING A PAYMENT METHOD

                int choice = (int) data.getSerializableExtra(ChoosePaymentFragment.EXTRA_CHOICE);
                Products.get(getContext()).setPaymentMethod(choice);
                getActivity().invalidateOptionsMenu();
                break;
            }
            case 3: {       // DELETING RESERVATIONS

                final int choice = (int) data.getSerializableExtra(ManageReservationsFragment.EXTRA_CHOICE);

                if (choice == -1) break;

                progressCircle = new ProgressDialog(getContext());
                progressCircle.setCancelable(true);
                progressCircle.setMessage("Cancellation in progress...");
                progressCircle.setIndeterminate(true);
                progressCircle.show();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();
                cancelled = false;
                final DatabaseReference mRRef = myRef.child("Products & Status").child(MyReservations.get().getMyReservations().get(choice).getVMId());
                mRRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++) {
                            String prodNumString = String.valueOf(i);
                            String prodCat = (String) dataSnapshot.child(prodNumString).child("productCategory").getValue();
                            String prodStat = (String) dataSnapshot.child(prodNumString).child("productStatus").getValue();
                            String prodCust = (String) dataSnapshot.child(prodNumString).child("customerID").getValue();
                            if (prodCat.equals(String.valueOf(Products.get(getContext()).getChosenProduct())) &&
                                    prodStat.equals("Reserved")) {
                                mRRef.child(prodNumString).child("customerID").setValue(null);
                                mRRef.child(prodNumString).child("productStatus").setValue("Available");
                                progressCircle.dismiss();
                                MyReservations.get().getMyReservations().remove(choice);
                                final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.success);
                                mp.start();
                                Toast.makeText(getContext(), "Reservation of: " + Products.get(getContext()).getProduct(MyReservations.get().
                                        getMyReservations().get(choice).getProductId()).getProductName() + " canceled!", Toast.LENGTH_SHORT).show();
                                getActivity().invalidateOptionsMenu();
                                cancelled = true;
                                break;
                            }
                        }
                        if (cancelled == false) {
                            progressCircle.dismiss();
                            final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.error);
                            mp.start();
                            Toast.makeText(getContext(), "Product no longer available!", Toast.LENGTH_LONG).show();
                        }
                    }
                    public void onCancelled(DatabaseError databaseError) { }
                });
                break;
            }
        }
    }
}
