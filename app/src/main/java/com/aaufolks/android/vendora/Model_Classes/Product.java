package com.aaufolks.android.vendora.Model_Classes;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by michalisgratsias on 27/10/16.
 */

public class Product {

    private int mProductId;
    private String mProductName;
    private int mProductPrice;
    private ImageData mProductPhotoData;

    public Product() {
    }

    public int getProductId() {
        return mProductId;
    }

    public void setProductId(int productId) {
        mProductId = productId;
    }

    public String getProductName() {
        return mProductName;
    }

    public void setProductName(String productName) {
        mProductName = productName;
    }

    public int getProductPrice() {
        return mProductPrice;
    }

    public void setProductPrice(int productPrice) {
        mProductPrice = productPrice;
    }

    public ImageData getProductPhotoData() {
        return mProductPhotoData;
    }

    public void setProductPhotoData(ImageData productPhotoData) {
        mProductPhotoData = productPhotoData;
    }

    public Bitmap getBitmapFromAsset(Context context, String filePath) {    // creates a Bitmap from photos in Assets
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
