package com.aaufolks.android.vendora.Controller_Classes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaufolks.android.vendora.Model_Classes.MyReservations;
import com.aaufolks.android.vendora.Model_Classes.Products;
import com.aaufolks.android.vendora.Model_Classes.Reservation;
import com.aaufolks.android.vendora.Model_Classes.VendingMachine;
import com.aaufolks.android.vendora.Pop_up_screens.ChoosePaymentFragment;
import com.aaufolks.android.vendora.Pop_up_screens.ManageReservationsFragment;
import com.aaufolks.android.vendora.Pop_up_screens.ReserveProductFragment;
import com.aaufolks.android.vendora.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dk.danskebank.mobilepay.sdk.CaptureType;
import dk.danskebank.mobilepay.sdk.Country;
import dk.danskebank.mobilepay.sdk.MobilePay;
import dk.danskebank.mobilepay.sdk.ResultCallback;
import dk.danskebank.mobilepay.sdk.model.FailureResult;
import dk.danskebank.mobilepay.sdk.model.Payment;
import dk.danskebank.mobilepay.sdk.model.SuccessResult;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import static com.aaufolks.android.vendora.R.drawable.pay_with_visa;
import static com.aaufolks.android.vendora.R.drawable.pay_with_paypal;
import static com.aaufolks.android.vendora.R.drawable.pay_with_mobile_pay;
import static com.aaufolks.android.vendora.R.drawable.reservations_0;
import static com.aaufolks.android.vendora.R.drawable.reservations_1;
import static com.aaufolks.android.vendora.R.drawable.reservations_2;
import static com.aaufolks.android.vendora.R.drawable.reservations_3;
import static com.aaufolks.android.vendora.R.drawable.reservations_4;
import static com.aaufolks.android.vendora.R.drawable.reservations_5;

/**
 * Created by michalisgratsias on 2/11/16.
 * Rewritten by michalisgratsias on 16/10/17.
 */

public class VMFragment extends SupportMapFragment
        implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private final int MAX_VMs_TO_SHOW = 5;
    private final int VM_DETECTION_DISTANCE = 20;
    private final int LOCATION_UPDATE_INTERVAL = 10000;
    private static final int MOBILEPAY_PAYMENT_REQUEST_CODE = 4;
    private int VMs_TO_SHOW;
    private ArrayList<VendingMachine> mVMs;
    private GoogleApiClient mClient;                        // this is for Google Play Services
    private GoogleMap mMap;                                 // a Google Map object
    private ArrayList<VendingMachine> vmsWProduct;
    private VendingMachine[] mMapItem = new VendingMachine[MAX_VMs_TO_SHOW];
    private Location mCurrentLocation;
    private String productName;
    private int productPrice;
    private Reservation mReservation;
    private Marker mLastSelectedMarker;
    private int mLastSelectedMarkerIndex;
    private LocationListener mListener;
    private MarkerOptions[] vmMarkerOptions = new MarkerOptions[MAX_VMs_TO_SHOW];
    private Marker[] vmMarker = new Marker[MAX_VMs_TO_SHOW];
    private boolean alreadySetup;
    private static final String ARG_VMs = "vms";
    private ProgressDialog progressCircle;
    private boolean reserved;
    private boolean cancelled;
    private Payment mPayment;

    // we use a method to create this Fragment (instead of using Constructor) and pass Arguments
    public static VMFragment newInstance(ArrayList<VendingMachine> vms) {
        Bundle args = new Bundle();                         // creates Bundle for arguments
        args.putSerializable(ARG_VMs, vms);                 // adds vms to Bundle
        VMFragment fragment = new VMFragment();             // creates Fragment instance
        fragment.setArguments(args);                        // sets Arguments
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show ActionBar Title and Menu
        ((VMActivity)getActivity()).getSupportActionBar().setTitle("Vending Machines");
        setHasOptionsMenu(true);

        // Access Fragment arguments for the VMs with the Product
        vmsWProduct = (ArrayList<VendingMachine>) getArguments().getSerializable(ARG_VMs);
        productName = Products.get(getContext()).getProduct(Products.get(getContext()).getChosenProduct()).getProductName();
        productPrice = Products.get(getContext()).getProduct(Products.get(getContext()).getChosenProduct()).getProductPrice();

        // Initialize the AppSwitch SDK with your own Merchant ID.
        // A country can also be provided to target specific MobilePay apps (default is DK).
        // It is important that init() is called before everything else since it resets all settings.
        MobilePay.getInstance().init(getString(R.string.merchant_id_generic), Country.DENMARK);

        // Use AppSwitch SDK global settings to tweak the payment flow, as required.
        tweakPaymentSettings();

        // CREATE A GOOGLE-API CLIENT AND A MAP OBJECT & FIND ALL LOCATIONS
        mClient = new GoogleApiClient.Builder(getActivity())            // create a client
                .addApi(LocationServices.API)                           // for Location Services API
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        alreadySetup = false;
                        findLocation();                                 // find Locations
                        setupUI();                                      // sets up UI when vms are found
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {                                     // gets a thread of a map object
                mMap = googleMap;
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                mMap.setInfoWindowAdapter(new PopupAdapter(layoutInflater));
                setupUI();                                              // sets up UI when map is first received
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // connect to GoogleAPIClient for Location Services & update visible state of menu button
        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        // disconnect from GoogleAPIClient for Loc.Services when not visible
        mClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // update Menu, find location, update InfoWindow, when on foreground
        getActivity().invalidateOptionsMenu();
        if (mClient.isConnected()) findLocation();
        if (mLastSelectedMarker != null && mLastSelectedMarker.isInfoWindowShown()) {mLastSelectedMarker.showInfoWindow();}
    }

    @Override
    public void onPause() {
        super.onPause();
        // disconnect Location services when on background
        LocationServices.FusedLocationApi.removeLocationUpdates(mClient, mListener);
    }

    //              ***            S H O W I N G      M E N U    I C O N S           ***
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        // Set the menu icons
        switch (MyReservations.get().getMyReservations().size()) {
            case 0: {menu.findItem(R.id.manage_reservations).setIcon(reservations_0); break;}
            case 1: {menu.findItem(R.id.manage_reservations).setIcon(reservations_1); break;}
            case 2: {menu.findItem(R.id.manage_reservations).setIcon(reservations_2); break;}
            case 3: {menu.findItem(R.id.manage_reservations).setIcon(reservations_3); break;}
            case 4: {menu.findItem(R.id.manage_reservations).setIcon(reservations_4); break;}
            case 5: {menu.findItem(R.id.manage_reservations).setIcon(reservations_5); break;}
        }
        switch (Products.get(getContext()).getPaymentMethod()) {
            case 0: {menu.findItem(R.id.choose_payment).setIcon(pay_with_visa); break;}
            case 1: {menu.findItem(R.id.choose_payment).setIcon(pay_with_paypal); break;}
            case 2: {menu.findItem(R.id.choose_payment).setIcon(pay_with_mobile_pay); break;}
        }
    }

    //               ***           S H O W I N G        T H E        M A P            ***
    private void setupUI() {

        // SHOW VM-MARKERS AND ZOOM THE MAP
        if (mMap == null || mMapItem[0] == null || alreadySetup) {  //map & map items not null, and not set up yet
            return;
        }
        LatLng[] itemPoint = new LatLng[VMs_TO_SHOW];               // collects location of vending machines & user
        for (int i=0; i < VMs_TO_SHOW; i++) {
            itemPoint[i] = new LatLng(mMapItem[i].getVMLat(), mMapItem[i].getVMLon());
        }
        LatLng myPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(getBitmapFromAsset(getContext(), "icons/Vendora.png"));

        mMap.clear();                                   // map gets cleared of markers
        for (int i=0; i < VMs_TO_SHOW; i++) {
            vmMarkerOptions[i] = new MarkerOptions()    // creates marker objects (vms) and info
                    .position(itemPoint[i])
                    .icon(itemBitmap)
                    .draggable(true)
                    .title("VM: " + vmsWProduct.get(i).getVMName())
                    .snippet("Address : " + vmsWProduct.get(i).getVMAddress()
                            + "\n" + productName + " is " + (vmsWProduct.get(i).isProdAvailable() ? "AVAILABLE" : "RESERVED")
                            + "\nDistance from you : " + vmsWProduct.get(i).getDistanceFromYou() + " meters"
                            + "\nTAP TO MAKE A RESERVATION");
            vmMarker[i] = mMap.addMarker(vmMarkerOptions[i]);
        }

        mMap.setOnInfoWindowClickListener(this);        // enables Listeners for Markers (vending machines)
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setMyLocationEnabled(true);

        LatLngBounds.Builder boundsBR = new LatLngBounds.Builder()  // creates bounds builder for map zooming
                .include(myPoint);
        for (int i=0; i < VMs_TO_SHOW; i++) {
            boundsBR = boundsBR.include(itemPoint[i]);              // User Location to be included in bounds rectangle
        }
        LatLngBounds bounds = boundsBR.build();                     // builds bounds

        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        // adjusts position and zoom of map at a rectangle around a set of points,
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);         // and animates camera to it
        alreadySetup = true;
    }

    //  CREATE CUSTOM INFO WINDOW FOR MARKERS
    class PopupAdapter implements InfoWindowAdapter {

        LayoutInflater inflater = null;

        PopupAdapter(LayoutInflater inflater) { this.inflater=inflater;}

        @Override
        public View getInfoWindow(Marker marker) {
            View popup = inflater.inflate(R.layout.map_popup, null);
            ((ImageView) popup.findViewById(R.id.icon)).setImageBitmap(getBitmapFromAsset(getContext(),
                    Products.get(getContext()).getProduct(Products.get(getContext()).getChosenProduct()).getProductPhotoData().getAssetPath()));
            TextView tv=(TextView)popup.findViewById(R.id.title);
            tv.setText(marker.getTitle());
            tv=(TextView)popup.findViewById(R.id.snippet);
            tv.setText(marker.getSnippet());
            return(popup);
        }

        @Override
        public View getInfoContents(Marker marker) {
            return(null);
        }
    }
    // WHEN INFO WINDOW IS CLICKED - SHOW RESERVATION POP-UP
    @Override
    public void onInfoWindowClick(Marker marker) {
        if (Products.get(getContext()).getPaymentMethod() != 2) {
            Toast.makeText(getContext(), "Please choose a different Payment method.", Toast.LENGTH_LONG).show();
            return;
        }
        if (MyReservations.get().getMyReservations().size() < 5) {
            ReserveProductFragment dialog1 = ReserveProductFragment.newInstance(productName, vmsWProduct.get(mLastSelectedMarkerIndex).getVMName());
            FragmentManager manager1 = getFragmentManager();
            dialog1.setTargetFragment(VMFragment.this, 1);
            dialog1.show(manager1, "DialogReserveProduct");
        }
        else {
            Toast.makeText(getContext(), "You have exceeded the maximum amount of Reservations. "
                    + "Try to Manage your Reservations from the Toolbar.", Toast.LENGTH_LONG).show();
        }
    }
    // WHEN MARKER IS CLICKED - Keep track of last Marker and its Index
    @Override
    public boolean onMarkerClick(Marker marker) {
        mLastSelectedMarker = marker;
        int index = 0;
        for (int i=0; i < VMs_TO_SHOW; i++) {if (vmMarker[i].equals(marker)) index = i;}
        mLastSelectedMarkerIndex = index;
        return false;                // Also Center and Show info window (as default)
    }
    // WHEN MARKER IS DRAGGED
    @Override
    public void onMarkerDrag(Marker marker) {
        // code N/A
    }
    // WHEN MARKER DRAG STARTS - Keep track of last Marker and its Index
    @Override
    public void onMarkerDragStart(Marker marker) {
        mLastSelectedMarker = marker;
        int index = 0;
        for (int i=0; i < VMs_TO_SHOW; i++) {if (vmMarker[i].equals(marker)) index = i;}
        mLastSelectedMarkerIndex = index;
    }
    // WHEN MARKER DRAG ENDS - Change marker coordinates and position
    @Override
    public void onMarkerDragEnd(Marker marker) {
        vmsWProduct.get(mLastSelectedMarkerIndex).setVMLat(marker.getPosition().latitude);
        vmsWProduct.get(mLastSelectedMarkerIndex).setVMLon(marker.getPosition().longitude);
        marker.setPosition(marker.getPosition());
    }
    // Create updates for User location and distances to vms
    private void findLocation() {
        LocationRequest request = LocationRequest.create();             // for location fix
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);    // use gps first
        request.setInterval(LOCATION_UPDATE_INTERVAL);                  // set interval between updates
        mListener = new LocationListener() {                            // set a Location change Listener
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Vendora", "Got a location fix: " + location);
                findVMDistances(location);                              // find closest vms to your location
                setupUI();                                              // sets-up the UI if there are changes
                if (mLastSelectedMarker != null) {                      // updates info window
                    mLastSelectedMarker.setSnippet("Address : " + vmsWProduct.get(mLastSelectedMarkerIndex).getVMAddress()
                            + "\n" + productName + " is " + (vmsWProduct.get(mLastSelectedMarkerIndex).isProdAvailable() ? "AVAILABLE" : "RESERVED")
                            + "\nDistance from you : " + vmsWProduct.get(mLastSelectedMarkerIndex).getDistanceFromYou() + " meters"
                            + "\nTAP TO MAKE A RESERVATION");
                    if (mLastSelectedMarker.isInfoWindowShown()) mLastSelectedMarker.showInfoWindow();
                }
                // If VM is very close to user, change to "NFC" screen
                if (vmsWProduct.get(0).getDistanceFromYou() < VM_DETECTION_DISTANCE) {
                    for (int i = 0; i< MyReservations.get().getMyReservations().size(); i++) {
                        if (MyReservations.get().getMyReservations().get(i).getVMName().equals(vmsWProduct.get(0).getVMName())) {
                            Intent intent = NFCActivity.newIntent(getActivity(), vmsWProduct.get(0).getVMName(), vmsWProduct.get(0).getVMAddress()); //passes VMName + Address
                            startActivity(intent);
                            break;
                        }
                    }
                }
            }
        };
        LocationServices.FusedLocationApi                               // requests the location updates
                .requestLocationUpdates(mClient, request, mListener);
    }

    // CALCULATE WHICH VMs HAVE CLOSEST DISTANCES TO USER, SO THEY CAN BE SHOWN ON MAP
    public void findVMDistances(Location location) {

        Location mLocation = location;
        if (vmsWProduct.size() == 0) {
            return;
        }
        // Limit vms to be shown, to increase zoom
        VMs_TO_SHOW = Math.min(vmsWProduct.size(), MAX_VMs_TO_SHOW);

        // Calculate all distances of VMs from User's Location
        Location locationX = new Location("");
        for (int i=0; i<vmsWProduct.size(); i++){
            locationX.setLatitude(vmsWProduct.get(i).getVMLat());
            locationX.setLongitude(vmsWProduct.get(i).getVMLon());
            vmsWProduct.get(i).setDistanceFromYou((int)mLocation.distanceTo(locationX)); // and record them
        }
        // Sorts the vms by current distance from user
        Collections.sort(vmsWProduct, new Comparator<VendingMachine>() {    // sorts VMs based on smallest distance fron You
            @Override
            public int compare(VendingMachine v1, VendingMachine v2) {
                return v1.getDistanceFromYou() - v2.getDistanceFromYou();
            }
        });
        for (int i = 0; i < VMs_TO_SHOW; i++) {                             // passes the closest VMs to Map
            if (mMapItem[i] == null || !mMapItem[i].equals(vmsWProduct.get(i))) {   // if there are changes
                mMapItem[i] = vmsWProduct.get(i);
                alreadySetup = false;                                       // makes sure Map will be updated
            }
        }
        mCurrentLocation = mLocation;
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Actions to take on menu buttons click
        switch (item.getItemId()) {
            // Show dialog/pop-up for managing reservations
            case R.id.manage_reservations:
                Log.i("Vendora", "Reservations size: " + MyReservations.get().getMyReservations().size());
                if (MyReservations.get().getMyReservations().size()>0) {
                    ManageReservationsFragment dialog1 = new ManageReservationsFragment();
                    FragmentManager manager1 = getFragmentManager();
                    dialog1.setTargetFragment(VMFragment.this, 3);
                    dialog1.show(manager1, "DialogManageReservations");
                } else {
                    Toast.makeText(getContext(), "There are no Reservations to show!", Toast.LENGTH_SHORT).show();
                }
                return true;
            // Show dialog/pop-up for choosing payment
            case R.id.choose_payment:
                ChoosePaymentFragment dialog2 = new ChoosePaymentFragment();
                FragmentManager manager2 = getFragmentManager();
                dialog2.setTargetFragment(VMFragment.this, 2);
                dialog2.show(manager2, "DialogChoosePayment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Actions to take when pop-ups are completed
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 1: {       // CALLING MOBILE PAY FOR RESERVATION (FROM INFO WINDOW)

                // Create a new MobilePay Payment object.
                mPayment = new Payment();

                // Set the callback URL
                mPayment.setServerCallbackUrl("https://us-central1-luminous-torch-4376.cloudfunctions.net/addMessage");

                // Set the product name.
                mPayment.setProductName(productName);

                // Set the product price.
                mPayment.setProductPrice(BigDecimal.valueOf(productPrice));

                // Set BulkRef for this payment. Payments will be grouped under this tag.
                mPayment.setBulkRef(productName);

                // Set the order ID. This is your reference and should match your business case. Has to be unique.
                mPayment.setOrderId(UUID.randomUUID().toString());

                // Have the SDK create an Intent with the Payment object specified.
                Intent paymentIntent = MobilePay.getInstance().createPaymentIntent(mPayment);

                // Query the SDK to see if MobilePay is present on the system.
                boolean isMobilePayInstalled = MobilePay.getInstance().isMobilePayInstalled(getContext());

                // If we determine that MobilePay is installed we start an AppSwitch payment.
                // Else we could lead the user to Google Play to download the app.
                if (isMobilePayInstalled) {
                    // Call startActivityForResult with the Intent and a specific request code of your choice.
                    // Wait for the selected request code in OnActivityResult.
                    startActivityForResult(paymentIntent, MOBILEPAY_PAYMENT_REQUEST_CODE);
                } else {
                    // Inform the user that MobilePay is not installed and lead them to Google Play.
                    downloadMobilePayApp();
                }
                break;
            }
            case 2: {       // SETTING A PAYMENT METHOD (FROM MENU)

                int choice = (int) data.getSerializableExtra(ChoosePaymentFragment.EXTRA_CHOICE);
                Products.get(getContext()).setPaymentMethod(choice);
                getActivity().invalidateOptionsMenu();
                break;
            }
            case 3: {       // DELETING RESERVATIONS (FROM MENU)

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
                final DatabaseReference mRRef = myRef.child("Products & Status").child(MyReservations.get()
                        .getMyReservations().get(choice).getVMId());
                mRRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++) {
                            String prodNumString = String.valueOf(i);
                            String prodCat = (String) dataSnapshot.child(prodNumString).child("productCategory").getValue();
                            String prodStat = (String) dataSnapshot.child(prodNumString).child("productStatus").getValue();
                            String prodCust = (String) dataSnapshot.child(prodNumString).child("customerID").getValue();
                            if (prodCat.equals(String.valueOf(Products.get(getContext()).getChosenProduct())) && prodStat.equals("Reserved")) {
                                mRRef.child(prodNumString).child("customerID").setValue(null);
                                mRRef.child(prodNumString).child("productStatus").setValue("Available");
                                progressCircle.dismiss();
                                MyReservations.get().getMyReservations().remove(choice);
                                vmsWProduct.get(mLastSelectedMarkerIndex).setProdAvailable(true);
                                mLastSelectedMarker.setSnippet("Address : " + vmsWProduct.get(mLastSelectedMarkerIndex).getVMAddress()
                                        + "\n" + productName + " is " + (vmsWProduct.get(mLastSelectedMarkerIndex).isProdAvailable() ? "AVAILABLE" : "RESERVED")
                                        + "\nDistance from you : " + vmsWProduct.get(mLastSelectedMarkerIndex).getDistanceFromYou() + " meters"
                                        + "\nTAP TO MAKE A RESERVATION");
                                mLastSelectedMarker.showInfoWindow();
                                final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.success);
                                mp.start();
                                Toast.makeText(getContext(), "Reservation of: " + productName + " canceled!", Toast.LENGTH_SHORT).show();
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
            case MOBILEPAY_PAYMENT_REQUEST_CODE: { // HANDLING RESERVATION RESULT

                // We call the AppSwitch SDK with resultCode and data.
                // The SDK will handle any validation if needed and determine if the payment succeeded.
                MobilePay.getInstance().handleResult(resultCode, data, new ResultCallback() {
                    @Override
                    public void onSuccess(SuccessResult result) {
                        // The payment succeeded. SuccessResult object holds further information.

                        // The product can now be delivered to the customer.

                        showPaymentResultDialog(getString(R.string.payment_result_dialog_success_title),
                                getString(R.string.payment_result_dialog_success_message, result.getTransactionId()));

                        progressCircle = new ProgressDialog(getContext());
                        progressCircle.setCancelable(true);
                        progressCircle.setMessage("Reservation in progress...");
                        progressCircle.setIndeterminate(true);
                        progressCircle.show();

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference();
                        reserved = false;
                        final DatabaseReference mRRef = myRef.child("Products & Status").child(vmsWProduct.get(mLastSelectedMarkerIndex).getVMId());
                        mRRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++) {
                                    String prodNumString = String.valueOf(i);
                                    String prodCat = (String) dataSnapshot.child(prodNumString).child("productCategory").getValue();
                                    String prodStat = (String) dataSnapshot.child(prodNumString).child("productStatus").getValue();
                                    if (prodCat.equals(String.valueOf(Products.get(getContext()).getChosenProduct())) && prodStat.equals("Available")) {
                                        mRRef.child(prodNumString).child("customerID").setValue(Products.get(getContext()).getCustomerId());
                                        mRRef.child(prodNumString).child("productStatus").setValue("Reserved");

                                        progressCircle.dismiss();
                                        mReservation = new Reservation(Products.get(getContext()).getChosenProduct(),
                                                vmsWProduct.get(mLastSelectedMarkerIndex).getVMId(),
                                                vmsWProduct.get(mLastSelectedMarkerIndex).getVMName(),
                                                mPayment.getOrderId());
                                        MyReservations.get().getMyReservations().add(mReservation);
                                        vmsWProduct.get(mLastSelectedMarkerIndex).setProdAvailable(false);
                                        mLastSelectedMarker.setSnippet("Address : " + vmsWProduct.get(mLastSelectedMarkerIndex).getVMAddress()
                                                + "\n" + productName + " is " + (vmsWProduct.get(mLastSelectedMarkerIndex).isProdAvailable() ? "AVAILABLE" : "RESERVED")
                                                + "\nDistance from you : " + vmsWProduct.get(mLastSelectedMarkerIndex).getDistanceFromYou() + " meters"
                                                + "\nTAP TO MAKE A RESERVATION");
                                        mLastSelectedMarker.showInfoWindow();
                                        final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.success);
                                        mp.start();
                                        //Toast.makeText(getContext(), "Reservation made!", Toast.LENGTH_LONG).show();
                                        getActivity().invalidateOptionsMenu();
                                        reserved = true;
                                        break;
                                    }
                                }
                                if (reserved == false) {
                                    progressCircle.dismiss();
                                    final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.error);
                                    mp.start();
                                    Toast.makeText(getContext(), "Product no longer available!", Toast.LENGTH_LONG).show();
                                }
                            }
                            public void onCancelled(DatabaseError databaseError) { }
                        });
                    }

                    @Override
                    public void onFailure(FailureResult result) {
                        // The payment failed. FailureResult object holds further information.

                        // You should inform the user why the error happened.
                        // See the list of possible error codes for more information.

                        // Example of how to catch a specific MobilePay error code.
                        if (result.getErrorCode() == MobilePay.ERROR_RESULT_CODE_UPDATE_APP) {
                            // Notify the user to update MobilePay.
                            showPaymentResultDialog(getString(R.string.payment_result_dialog_error_update_title),
                                    getString(R.string.payment_result_dialog_error_update_message));
                            return;
                        }

                        showPaymentResultDialog(getString(R.string.payment_result_dialog_error_title,
                                String.valueOf(result.getErrorCode())), result.getErrorMessage());
                    }

                    @Override
                    public void onCancel() {
                        // The payment was cancelled, which means the user jumped back
                        // from MobilePay before processing the payment.
                        showPaymentResultDialog(getString(R.string.payment_result_dialog_cancelled_title),
                                getString(R.string.payment_result_dialog_cancelled_message));
                    }
                });
                break;
            }

        }
    }

    private void showPaymentResultDialog(String title, String content) {
        // Show a simple dialog with information of the transaction.
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton(getString(R.string.payment_result_dialog_positive), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void downloadMobilePayApp() {
        // Simple dialog informing the user about the missing MobilePay app and offering them to install it from Google Play.
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.install_mobilepay_dialog_title))
                .setMessage(getString(R.string.install_mobilepay_dialog_message))
                .setPositiveButton(getString(R.string.install_mobilepay_dialog_positive_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Create a MobilePay download Intent.
                        Intent intent = MobilePay.getInstance().createDownloadMobilePayIntent(getContext());
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.install_mobilepay_dialog_negative_text), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public Bitmap getBitmapFromAsset(Context context, String filePath) {
        // Creates Bitmap for vm icons
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            Log.i("Vendora: ", "IOException");
        }
        return bitmap;
    }

    private void tweakPaymentSettings() {
        // Determines which type of payment you would like to start. CAPTURE, RESERVE and PARTIAL CAPTURE are the possibilities.
        // CAPTURE is default. See the GitHub wiki for more information on each type.
        MobilePay.getInstance().setCaptureType(CaptureType.RESERVE);
        // Set the number of seconds from the MobilePay receipt are shown to the user returns to the merchant app. Default is 1.
        MobilePay.getInstance().setReturnSeconds(1);
        // Set the number of seconds the user has to complete the payment. Default is 0, which is no timeout.
        MobilePay.getInstance().setTimeoutSeconds(0);
    }
}
