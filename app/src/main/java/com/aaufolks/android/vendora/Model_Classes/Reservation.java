package com.aaufolks.android.vendora.Model_Classes;

/**
 * Created by michalisgratsias on 12/11/2016.
 */

public class Reservation {

    private int mProductId;
    private String mVMId;
    private String mVMName;

    public Reservation(int productId, String vmId, String vmName) {
        mProductId = productId;
        mVMId = vmId;
        mVMName = vmName;
    }

    public int getProductId() {
        return mProductId;
    }

    public void setProductId(int productId) {
        mProductId = productId;
    }

    public String getVMId() {
        return mVMId;
    }

    public void setVMId(String VMId) {
        mVMId = VMId;
    }

    public String getVMName() {
        return mVMName;
    }

    public void setVMName(String VMName) {
        mVMName = VMName;
    }
}
