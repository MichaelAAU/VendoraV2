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
import android.widget.TextView;
import android.widget.Toast;

import com.aaufolks.android.vendora.Model_Classes.MyReservations;
import com.aaufolks.android.vendora.Model_Classes.Products;
import com.aaufolks.android.vendora.Pop_up_screens.ChoosePaymentFragment;
import com.aaufolks.android.vendora.Pop_up_screens.ManageReservationsFragment;
import com.aaufolks.android.vendora.R;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

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

public class NFCFragment extends Fragment {//implements CardEmulationService.DeliveryCallback {

    private static final String ARG_VM_NAME = "vm_name";
    private static final String ARG_VM_ADDRESS = "vm_address";
    String[] payment = new String[]{"Visa", "PayPal", "MobilePay"};
    private TextView mTextNFC1;
    private TextView mTextNFC2;
    private String vmName;
    private String vmAddress;
    private final int MY_SOCKET_TIMEOUT_MS = 10000;
    private ProgressDialog progressCircle;

    public static NFCFragment newInstance(String vmName, String vmAddress) {   // we use a method to create Fragment instead of using Constructor
        Bundle args = new Bundle();                         // creates Bundle for arguments
        args.putSerializable(ARG_VM_NAME, vmName);          // adds productID to Bundle
        args.putSerializable(ARG_VM_ADDRESS, vmAddress);
        NFCFragment fragment = new NFCFragment();             // creates Fragment instance
        fragment.setArguments(args);                        // sets Arguments
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {   // it is Public because it can be called by various activities hosting it

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        vmName = (String) getArguments().getSerializable(ARG_VM_NAME);   // accessing Fragment arguments for productName
        vmAddress = (String) getArguments().getSerializable(ARG_VM_ADDRESS);
        Products.get(getContext()).setCurrentMachine(vmName);
        ((NFCActivity)getActivity()).getSupportActionBar().setTitle("Find Machine");
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nfc, parent, false);
        mTextNFC1 = (TextView) view.findViewById(R.id.NFC_text1);
        mTextNFC2 = (TextView) view.findViewById(R.id.NFC_text2);

        mTextNFC1.setText("You are now less than 20 meters"
                    + "\naway from vending machine:\n"
                    + vmName
                    + "\nat " + vmAddress +".");
        mTextNFC2.setText("Please place the back of"
                + "\nyour phone by the above sign"
                + "\non the machine for 2 seconds"
                + "\nto pay with " + payment[Products.get(getContext()).getPaymentMethod()]
                + "\nand get your product!");
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
                    dialog1.setTargetFragment(NFCFragment.this, 3);
                    dialog1.show(manager1, "DialogManageReservations");
                } else {
                    Toast.makeText(getContext(), "There are no Reservations to show!", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.choose_payment:   //shows dialog for choosing payment
                ChoosePaymentFragment dialog2 = new ChoosePaymentFragment();
                FragmentManager manager2 = getFragmentManager();
                dialog2.setTargetFragment(NFCFragment.this, 2);
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
                mTextNFC2.setText("Please place and hold"
                        + "\nyour phone over the above sign"
                        + "\non the machine to pay with " + payment[Products.get(getContext()).getPaymentMethod()]
                        + "\nand get your product!");
                break;
            }
            case 3: {       // MANAGING RESERVATIONS

                final int choice = (int) data.getSerializableExtra(ManageReservationsFragment.EXTRA_CHOICE);

                if (choice == -1) break;

                final String myUrl4 = "http://uncomely-story.000webhostapp.com/public/upload.php?action=delete&vm_id="
                        + MyReservations.get().getMyReservations().get(choice).getVMId() + "&item_type_id="
                        + MyReservations.get().getMyReservations().get(choice).getProductId() + "&customer_id="
                        + Products.get(getContext()).getCustomerId() + "&lkey=250250250";

                progressCircle = new ProgressDialog(getContext());
                progressCircle.setCancelable(true);
                progressCircle.setMessage("Cancellation in progress...");
                progressCircle.setIndeterminate(true);
                progressCircle.show();

                //                  *** CANCELLING RESERVATIONS FROM DATABASE-SERVER    ***

                //creating a requestQueue object that holds http-request objects
                final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                requestQueue.start();

                //setting up the response listener in case of success & populating Products Array
                final Response.Listener<String> onSuccessListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressCircle.dismiss();
                        response = response.trim();
                        Log.d("Vendora", "My URL: " + myUrl4);
                        Log.d("Vendora", "CANCEL Reservation Responce: " + response);
                        if (response.contains("Reservation canceled")) {
                            MyReservations.get().getMyReservations().remove(choice);
                            final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.success);
                            mp.start();
                            Toast.makeText(getContext(), "Reservation of: " +
                                    Products.get(getContext()).getProduct(MyReservations.get().getMyReservations().get(choice).getProductId()).getProductName()
                                    + " canceled!", Toast.LENGTH_SHORT).show();
                            getActivity().invalidateOptionsMenu();
                        } else {
                            final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.error);
                            mp.start();
                            Toast.makeText(getContext(), "Reservation cancelation NOT successful!", Toast.LENGTH_SHORT).show();
                        }
                        requestQueue.stop();
                    }
                };
                //setting up the response listener in case of error
                final Response.ErrorListener onErrorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressCircle.dismiss();
                        error.printStackTrace();
                        requestQueue.stop();
                    }
                };

                // request a String response from the provided url
                StringRequest stringRequest = new StringRequest(Request.Method.GET, myUrl4, onSuccessListener, onErrorListener);

                // set a timeout for the request
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        MY_SOCKET_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                Log.d("Tag", "Adding request: " + myUrl4);
                // add the request object to the request queue to get it serviced and delivered back

                requestQueue.add(stringRequest);
                break;
            }
        }
    }
}
