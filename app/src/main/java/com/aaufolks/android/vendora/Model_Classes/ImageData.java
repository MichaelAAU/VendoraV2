package com.aaufolks.android.vendora.Model_Classes;

/**
 * Created by michalisgratsias on 28/10/16.
 */

public class ImageData {

    private Integer mImageId;
    private String mName;
    private String mAssetPath;

    public ImageData(String assetPath) {    // creates the filename from the path
        mAssetPath = assetPath;
        String[] components = assetPath.split("/");
        String filename = components[components.length-1];
        mName = filename.replace(".png",  "");
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    public String getName() {
        return mName;
    }

    public Integer getImageId() {
        return mImageId;
    }

    public void setImageId(Integer imageId) {
        mImageId = imageId;
    }
}
