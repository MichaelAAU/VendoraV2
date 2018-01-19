package com.aaufolks.android.vendora.Controller_Classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import android.support.v4.app.Fragment;                     // from support library
import android.support.v7.widget.LinearLayoutManager;       // from support library
import android.support.v7.widget.RecyclerView;              // from support library
import android.widget.Toast;

import com.aaufolks.android.vendora.Model_Classes.MyReservations;
import com.aaufolks.android.vendora.Model_Classes.Product;
import com.aaufolks.android.vendora.Model_Classes.Products;
import com.aaufolks.android.vendora.Model_Classes.VMs;
import com.aaufolks.android.vendora.Model_Classes.VendingMachine;
import com.aaufolks.android.vendora.Pop_up_screens.ChoosePaymentFragment;
import com.aaufolks.android.vendora.Pop_up_screens.FirstPaymentFragment;
import com.aaufolks.android.vendora.Pop_up_screens.ManageReservationsFragment;
import com.aaufolks.android.vendora.R;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.aaufolks.android.vendora.R.drawable.pay_with_cash;
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
 * Created by michalisgratsias on 26/10/2016.
 * Rewritten by michalisgratsias on 9/11/2017.
 */

public class ProductFragment extends Fragment {

    private static final String ARG_ID_TOKEN = "id_token";
    public RecyclerView mProductRecyclerView;         // RecyclerView creates only enough views to fill the screen and scrolls them
    private ProductAdapter mAdapter;                  // Adapter controls the data to be displayed by RecyclerView
    private ProgressDialog progressCircle;
    private boolean cancelled;

    // We use a method to create this Fragment (instead of using Constructor) and pass Arguments
    public static ProductFragment newInstance(IdpResponse response) {
        Bundle args = new Bundle();                         // creates Bundle for arguments
        if (response != null) {
            String idToken = response.getIdpToken();
            Log.d("Tag", "Id_Token: " + idToken);
            args.putString(ARG_ID_TOKEN, idToken);          // adds id_token to Bundle
        }
        ProductFragment fragment = new ProductFragment();   // creates Fragment instance
        fragment.setArguments(args);                        // sets Arguments
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {   // it is Public because it can be called by various activities hosting it
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("Tag", "Name: " + user.getDisplayName());
        Log.d("Tag", "Email: " + user.getEmail());
        Log.d("Tag", "Phone: " + user.getPhoneNumber());
        if (user.getPhotoUrl() != null) {Log.d("Tag", "Photo: " + "Has photo");
        } else Log.d("Tag", "Photo: " + "null");
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        mProductRecyclerView = (RecyclerView)view.findViewById(R.id.lists_recycler_view);
        mProductRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));    // handles the positioning of items and defines the scrolling behaviour
        updateUI();     // sets up the UI
        return view;
    }

    private void updateUI() {
        ((ProductActivity)getActivity()).getSupportActionBar().setTitle("Available Products");
        if (mAdapter == null) {
            mAdapter = new ProductAdapter(Products.get(getContext()).getProducts());    // gives products list to adapter
            mProductRecyclerView.setAdapter(mAdapter);}                                 // connects to recycler view
        else {mAdapter.notifyDataSetChanged();}                                         // if adapter existing, updates data changes
    }

    private class ProductHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // viewholder class holds reference to the entire view passed to super(view)
        private Product mProduct;
        private CardView mCard;
        private ImageView mProductPhoto;
        private TextView mProductNameTextView;
        private TextView mProductPriceTextView;

        public ProductHolder(View itemView) {                                           // constructor - stashes the views
            super(itemView);
            itemView.setOnClickListener(this);
            mCard = (CardView) itemView.findViewById(R.id.card);
            mProductPhoto = (ImageView) itemView.findViewById(R.id.product_imageView);
            mProductNameTextView = (TextView) itemView.findViewById(R.id.product_name);
            mProductPriceTextView = (TextView) itemView.findViewById(R.id.product_price);
        }

        public void bindList(Product product) {                                         // list data entered in fragment viewholder
            mProduct = product;
            mProductPhoto.setImageBitmap(mProduct.getBitmapFromAsset(getContext(), mProduct.getProductPhotoData().getAssetPath()));
            mProductNameTextView.setText(mProduct.getProductName());
            mProductPriceTextView.setText(mProduct.getProductPrice() + " kr");
        }

        @Override
        public void onClick(View v) {
            Products.get(getContext()).setChosenProduct(mProduct.getProductId());
            if (Products.get(getContext()).getPaymentMethod() == -1) {
                FirstPaymentFragment dialog2 = new FirstPaymentFragment();
                FragmentManager manager2 = getFragmentManager();
                dialog2.show(manager2, "DialogChoosePayment");
            }
            else {
                new VMs(getContext()).getVMs();                                         // on click, gets all the VMs that have the chosen product
            }
        }
    }

    private class ProductAdapter extends RecyclerView.Adapter<ProductHolder> {          // adapter class
                                                                                        // creates needed viewholders, binds them to the data
        private ArrayList<Product> mProducts;

        public ProductAdapter(ArrayList<Product> products) {        // constructor
            mProducts = products;
        }

        @Override
        public ProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {                           // needs new view
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.fragment_product_list_item, parent, false);         // creates view & wraps it in a viewholder
            return new ProductHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductHolder holder, int position) {                                  // binds viewholder's view to a model object
            Product product = mProducts.get(position);
            holder.bindList(product);
        }

        @Override
        public int getItemCount() {
            return mProducts.size();
        }
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
            case -1: {
                menu.findItem(R.id.choose_payment).setIcon(pay_with_cash);
                break;
            }
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
                    dialog1.setTargetFragment(ProductFragment.this, 3);
                    dialog1.show(manager1, "DialogManageReservations");
                } else {
                    Toast.makeText(getContext(), "There are no Reservations to show!", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.choose_payment:   //shows dialog for choosing payment
                ChoosePaymentFragment dialog2 = new ChoosePaymentFragment();
                FragmentManager manager2 = getFragmentManager();
                dialog2.setTargetFragment(ProductFragment.this, 2);
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
                            if (prodCat.equals(String.valueOf(MyReservations.get().getMyReservations().get(choice).getProductId())) &&
                                    (prodStat.equals("Reserved") || prodStat.equals("Hold")) &&
                                    prodCust.equals(Products.get(getContext()).getCustomerId())) {
                                mRRef.child(prodNumString).child("customerID").setValue(null);
                                mRRef.child(prodNumString).child("productStatus").setValue("Available");
                                mRRef.child(prodNumString).child("orderID").setValue(null);
                                progressCircle.dismiss();
                                final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.success);
                                mp.start();
                                Toast.makeText(getContext(), "Reservation of: " + Products.get(getContext()).getProduct(MyReservations.get().
                                        getMyReservations().get(choice).getProductId()).getProductName() + " is canceled!", Toast.LENGTH_SHORT).show();
                                getActivity().invalidateOptionsMenu();
                                MyReservations.get().getMyReservations().remove(choice);
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
