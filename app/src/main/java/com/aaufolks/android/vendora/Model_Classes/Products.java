package com.aaufolks.android.vendora.Model_Classes;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.provider.Settings.Secure;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static java.lang.Integer.parseInt;

/**
 * Created by michalisgratsias on 27/10/16.
 * Rewritten by michalisgratsias on 16/10/17.
 */

public class Products {

    private static final String IMAGES_FOLDER = "product_pics";
    private ArrayList<Product> mProducts;
    private ArrayList<String> mAvailable;
    private static Products sProducts;
    private AssetManager mAssets;
    private String customerId;
    private int paymentMethod;
    private int chosenProduct;
    private String currentMachine;
    private Reservation mReservation;
    private ArrayList<ImageData> mImagesData = new ArrayList<>();

    public Products(final Context context) {
        Log.d("Tag", "Creating products");
        mProducts = new ArrayList<Product>();
        mAvailable = new ArrayList<String>();
        mProducts.clear();
        mAvailable.clear();
        customerId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        paymentMethod = -1;
        mAssets = context.getAssets();
        loadImageData();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();

        DatabaseReference m1Ref = myRef.child("Products & Status");
        m1Ref.addChildEventListener(new ChildEventListener() {
            @Override // Retrieve all available product IDs as they are in the database from all VMs
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                String vmID = (String) snapshot.getKey();
                Log.d("Tag", "vmId: " + vmID);
                for (int i = 1; i <= snapshot.getChildrenCount(); i++) {
                    String prodNumString = String.valueOf(i);
                    Log.d("Tag", "prod: " + snapshot.child(prodNumString).getKey());
                    String prodCat = (String) snapshot.child(prodNumString).child("productCategory").getValue();
                    String prodStat = (String) snapshot.child(prodNumString).child("productStatus").getValue();
                    if (prodStat.equals("Available") && !mAvailable.contains(prodCat)) {
                        mAvailable.add(prodCat);
                    }
                    if (prodStat.equals("Hold") || prodStat.equals("Reserved")) {
                        String prodCustomer = (String) snapshot.child(prodNumString).child("customerID").getValue();
                        if (prodCustomer.equals(customerId)) {
                            mReservation = new Reservation(parseInt(prodCat), vmID, "Unknown VM", "Unknown");
                            Log.d("Tag", "vmId: " + vmID + " prodId: " + parseInt(prodCat));
                            MyReservations.get().getMyReservations().add(mReservation);
                        }
                        if (!mAvailable.contains(prodCat)) {
                            mAvailable.add(prodCat);
                        }
                    }
                }
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override public void onCancelled(DatabaseError databaseError) {Log.w(TAG, "loadPost:onCancelled", databaseError.toException());}
        });

        DatabaseReference m2Ref = myRef.child("Product Categories");
        m2Ref.addChildEventListener(new ChildEventListener() {
            @Override // Retrieve AVAILABLE product categories and prices
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                String prodID = (String) snapshot.getKey();
                String prodName = (String) snapshot.child("productName").getValue();
                long prodPrice = (long) snapshot.child("productPrice").getValue();
                if (mAvailable.contains(prodID)) {
                    Product product = new Product();
                    product.setProductId(parseInt(prodID));
                    Log.d("Tag", "Id: " + product.getProductId());
                    product.setProductName(prodName);
                    Log.d("Tag", "Name: " + product.getProductName());
                    ImageData image = mImagesData.get(product.getProductId()-1);
                    Log.d("Tag", "Asset path: " + mImagesData.get(product.getProductId()-1).getAssetPath());
                    product.setProductPhotoData(mImagesData.get(product.getProductId()-1));
                    product.setProductPrice((int) prodPrice);
                    Log.d("Tag", "Price: " + product.getProductPrice());
                    mProducts.add(product);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    public static Products get(Context context) {             // creates list as a Singleton= only 1 Products object possible
        if (sProducts == null) {
            sProducts = new Products(context);
        }
        else {Log.d("Tag", "Products: " + sProducts.toString());}
        return sProducts;
    }

    public Product getProduct(int id) {                                    // get a Products-List by ID
        for (Product p : mProducts) {
            if (p.getProductId() == id)
                return p;
        }
        return null;
    }

    public ArrayList<Product> getProducts() {                         // get all Products Lists
        return mProducts;
    }        // get all Products-Lists

    private void loadImageData() {
        String[] imageNames;
        try {
            imageNames = mAssets.list(IMAGES_FOLDER);
        }
        catch (IOException ioe) {
            Log.e("Vendora", "Could not list assets", ioe);
            return;
        }
        mImagesData.clear();
        for (int i=0; i<imageNames.length; i++) {
            String assetPath = IMAGES_FOLDER + "/" + imageNames[i];
            //Log.d("Vendora", i+ " Picture name: " + assetPath);
            ImageData imageData = new ImageData(assetPath);
            mImagesData.add(imageData);
        }
    }

    public int getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(int paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getChosenProduct() {
        return chosenProduct;
    }

    public void setChosenProduct(int chosenProduct) {
        this.chosenProduct = chosenProduct;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCurrentMachine() {
        return currentMachine;
    }

    public void setCurrentMachine(String currentMachine) {
        this.currentMachine = currentMachine;
    }
}
