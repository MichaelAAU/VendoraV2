package com.aaufolks.android.vendora.Controller_Classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.WindowManager;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
 */

public class VMFragment extends SupportMapFragment
        implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private final int MAX_VMs_TO_SHOW = 5;
    private final int VM_DETECTION_DISTANCE = 20;
    private final int LOCATION_UPDATE_INTERVAL = 10000;
    private int VMs_TO_SHOW;
    private ArrayList<VendingMachine> mVMs;
    private GoogleApiClient mClient;                        // this is for Google Play Services
    private GoogleMap mMap;                                 // a Google Map object
    private ArrayList<VendingMachine> vmsWProduct;
    private VendingMachine[] mMapItem = new VendingMachine[MAX_VMs_TO_SHOW];
    private Location mCurrentLocation;
    private String productName;
    private Reservation mReservation;
    private Marker mLastSelectedMarker;
    private int mLastSelectedMarkerIndex;
    private LocationListener mListener;
    private MarkerOptions[] vmMarkerOptions = new MarkerOptions[MAX_VMs_TO_SHOW];
    private Marker[] vmMarker = new Marker[MAX_VMs_TO_SHOW];
    private boolean alreadySetup;
    private static final String ARG_VMs = "vms";
    private final int MY_SOCKET_TIMEOUT_MS = 10000;
    private ProgressDialog progressCircle;
    private boolean reserved;
    private boolean cancelled;

    public static VMFragment newInstance(ArrayList<VendingMachine> vms) {   // we use a method to create Fragment instead of using Constructor
        Bundle args = new Bundle();                         // creates Bundle for arguments
        args.putSerializable(ARG_VMs, vms);                 // adds vms to Bundle
        VMFragment fragment = new VMFragment();             // creates Fragment instance
        fragment.setArguments(args);                        // sets Arguments
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {   // CREATE GOOGLE API CLIENT AND MAP OBJECT & FIND LOCATIONS
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        vmsWProduct = (ArrayList<VendingMachine>) getArguments().getSerializable(ARG_VMs);   // accessing Fragment arguments for productName
        productName = Products.get(getContext()).getProduct(Products.get(getContext()).getChosenProduct()).getProductName();
        ((VMActivity)getActivity()).getSupportActionBar().setTitle("Vending Machines");

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
        getActivity().invalidateOptionsMenu();  // update visible state of menu button
        mClient.connect();                      // connect to GoogleAPIClient for Loc.Services
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();                   // disconnect from GoogleAPIClient for Loc.Services when not visible
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        if (mClient.isConnected()) findLocation();
        if (mLastSelectedMarker != null && mLastSelectedMarker.isInfoWindowShown()) {mLastSelectedMarker.showInfoWindow();}
    }

    @Override
    public void onPause() {
        super.onPause();                        // disconnect Location services when on background
        LocationServices.FusedLocationApi.removeLocationUpdates(mClient, mListener);
    }

    //              ***             M E N U     C O D E             ***
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);   //sets the menu icons
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
    public boolean onOptionsItemSelected(MenuItem item) {   //actions of menu buttons
        switch (item.getItemId()) {
            case R.id.manage_reservations:      //shows dialog/pop-up for managing reservations
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
            case R.id.choose_payment:           //shows dialog/pop-up for choosing payment
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
        if (resultCode != Activity.RESULT_OK) { // actions when pop-ups are completed
            return;
        }
        switch (requestCode) {
            case 1: {       // MAKING A RESERVATION

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
                            if (prodCat.equals(String.valueOf(Products.get(getContext()).getChosenProduct())) &&
                                    prodStat.equals("Available")) {
                                mRRef.child(prodNumString).child("customerID").setValue(Products.get(getContext()).getCustomerId());
                                mRRef.child(prodNumString).child("productStatus").setValue("Reserved");
                                progressCircle.dismiss();
                                mReservation = new Reservation(Products.get(getContext()).getChosenProduct(),
                                        vmsWProduct.get(mLastSelectedMarkerIndex).getVMId(), vmsWProduct.get(mLastSelectedMarkerIndex).getVMName());
                                MyReservations.get().getMyReservations().add(mReservation);
                                vmsWProduct.get(mLastSelectedMarkerIndex).setProdAvailable(false);
                                mLastSelectedMarker.showInfoWindow();
                                final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.success);
                                mp.start();
                                Toast.makeText(getContext(), "Reservation made!", Toast.LENGTH_LONG).show();
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
                break;
            }
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
                                vmsWProduct.get(mLastSelectedMarkerIndex).setProdAvailable(true);
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
        }
    }
    //                              ***             MAP CODE            ***

    private void setupUI() {                                        // SHOW VM-MARKERS AND ZOOM THE MAP

        if (mMap == null || mMapItem[0] == null || alreadySetup) {  //need to have map, map items and no set up
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
            vmMarkerOptions[i] = new MarkerOptions()    // creates marker objects and info
                    .position(itemPoint[i])
                    .icon(itemBitmap)
                    .draggable(true)
                    .title(vmsWProduct.get(i).getVMName())
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
                .include(myPoint);                                  // Location to be included in bounds rectangle
        for (int i=0; i < VMs_TO_SHOW; i++) {
            boundsBR = boundsBR.include(itemPoint[i]);              // Location to be included in bounds rectangle
        }
        LatLngBounds bounds = boundsBR.build();                     // builds bounds

        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);  // adjusts position and zoom of map
        mMap.animateCamera(update);         // at a rectangle around a set of points, and animates camera to it
        alreadySetup = true;
    }

    class PopupAdapter implements InfoWindowAdapter {   //  CREATES CUSTOM INFO WINDOW FOR MARKERS

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

    @Override
    public void onInfoWindowClick(Marker marker) {  //      SHOWS RESERVATION POP-UP WHEN INFO CLICKED
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

    @Override
    public boolean onMarkerClick(Marker marker) {   // keep track of last Marker and Index
        mLastSelectedMarker = marker;
        int index = 0;
        for (int i=0; i < VMs_TO_SHOW; i++) {if (vmMarker[i].equals(marker)) index = i;}
        mLastSelectedMarkerIndex = index;
        return false;                               // show info window (default)
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // code N/A
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        mLastSelectedMarker = marker;
        int index = 0;
        for (int i=0; i < VMs_TO_SHOW; i++) {if (vmMarker[i].equals(marker)) index = i;}
        mLastSelectedMarkerIndex = index;
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {    // change marker position
        vmsWProduct.get(mLastSelectedMarkerIndex).setVMLat(marker.getPosition().latitude);
        vmsWProduct.get(mLastSelectedMarkerIndex).setVMLon(marker.getPosition().longitude);
        marker.setPosition(marker.getPosition());
    }

    private void findLocation() {               // Creates custom location updates
        LocationRequest request = LocationRequest.create();             // for location fix
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);    // use gps first
        request.setInterval(LOCATION_UPDATE_INTERVAL);                  // set interval between updates
        mListener = new LocationListener() {                            // set a Location change Listener
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Vendora", "Got a location fix: " + location);
                findVMDistances(location);                              // method finds closest vms to your location
                setupUI();                                              // setsup the UI if there are changes
                if (mLastSelectedMarker != null) {                      // updates info window
                    mLastSelectedMarker.setSnippet("Address : " + vmsWProduct.get(mLastSelectedMarkerIndex).getVMAddress()
                            + "\n" + productName + " is " + (vmsWProduct.get(mLastSelectedMarkerIndex).isProdAvailable() ? "AVAILABLE" : "RESERVED")
                            + "\nDistance from you : " + vmsWProduct.get(mLastSelectedMarkerIndex).getDistanceFromYou() + " meters"
                            + "\nTAP TO MAKE A RESERVATION");
                    mLastSelectedMarker.showInfoWindow();
                }
                if (vmsWProduct.get(0).getDistanceFromYou() < VM_DETECTION_DISTANCE) {  // Go to NFC screen if VM is close to user
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
        LocationServices.FusedLocationApi                               // requests the location fixes/updates
                .requestLocationUpdates(mClient, request, mListener);
    }

    public void findVMDistances(Location location) {    // CALCULATES CLOSEST DISTANCES TO VMs

        Location mLocation = location;
        if (vmsWProduct.size() == 0) {
            return;
        }
        VMs_TO_SHOW = Math.min(vmsWProduct.size(), MAX_VMs_TO_SHOW);

        Location locationX = new Location("");                  // calculates all distances of VMs from Your Location
        for (int i=0; i<vmsWProduct.size(); i++){
            locationX.setLatitude(vmsWProduct.get(i).getVMLat());
            locationX.setLongitude(vmsWProduct.get(i).getVMLon());
            vmsWProduct.get(i).setDistanceFromYou((int)mLocation.distanceTo(locationX)); // and records them
        }

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

    public Bitmap getBitmapFromAsset(Context context, String filePath) {    // Creates Bitmap for vm icons
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
}
