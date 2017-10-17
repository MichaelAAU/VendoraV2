package com.aaufolks.android.vendora.Controller_Classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
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
import com.aaufolks.android.vendora.Pop_up_screens.ChoosePaymentFragment;
import com.aaufolks.android.vendora.Pop_up_screens.FirstPaymentFragment;
import com.aaufolks.android.vendora.Pop_up_screens.ManageReservationsFragment;
import com.aaufolks.android.vendora.R;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
 */

public class ProductFragment extends Fragment {

    public RecyclerView mProductRecyclerView;         // RecyclerView creates only enough views to fill the screen and scrolls them
    private ProductAdapter mAdapter;                  // Adapter controls the data to be displayed by RecyclerView
    private final int MY_SOCKET_TIMEOUT_MS = 10000;
    private ProgressDialog progressCircle;

    @Override
    public void onCreate(Bundle savedInstanceState) {   // it is Public because it can be called by various activities hosting it
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
