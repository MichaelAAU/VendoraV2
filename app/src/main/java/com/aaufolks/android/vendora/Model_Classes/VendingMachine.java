package com.aaufolks.android.vendora.Model_Classes;

/**
 * Created by michalisgratsias on 3/11/16.
 */

public class VendingMachine implements java.io.Serializable {
    private String mVMId;
    private String mVMName;
    private String mVMAddress;
    private double mVMLat;
    private double mVMLon;
    private String mVMIP;
    private boolean prodAvailable;
    private int mDistanceFromYou;

    public String getVMAddress() {
        return mVMAddress;
    }

    public void setVMAddress(String address) {
        mVMAddress = address;
    }

    public String getVMName() {
        return mVMName;
    }

    public void setVMName(String name) {
        mVMName = name;
    }

    public String getVMId() {
        return mVMId;
    }

    public void setVMId(String id) {
        mVMId = id;
    }

    public double getVMLat() {
        return mVMLat;
    }

    public void setVMLat(double lat) {
        mVMLat = lat;
    }

    public double getVMLon() {
        return mVMLon;
    }

    public void setVMLon(double lon) {
        mVMLon = lon;
    }

    public int getDistanceFromYou() {return mDistanceFromYou;}

    public void setDistanceFromYou(int distanceFromYou) {mDistanceFromYou = distanceFromYou;}

    public String getVMIP() {
        return mVMIP;
    }

    public void setVMIP(String VMIP) {
        mVMIP = VMIP;
    }

    public boolean isProdAvailable() {
        return prodAvailable;
    }

    public void setProdAvailable(boolean prodAvailable) {
        this.prodAvailable = prodAvailable;
    }
}
