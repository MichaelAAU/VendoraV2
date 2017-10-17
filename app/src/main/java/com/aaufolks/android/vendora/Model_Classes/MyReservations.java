package com.aaufolks.android.vendora.Model_Classes;

import java.util.ArrayList;

/**
 * Created by michalisgratsias on 12/11/2016.
 */

public class MyReservations {

    private ArrayList<Reservation> mMyReservations;
    private static MyReservations sMyReservations;

    public MyReservations() {
        mMyReservations = new ArrayList<Reservation>();
    }

    public static MyReservations get() {            // creates list as a Singleton
        if (sMyReservations == null) {              // = only 1 VMs object possible
            sMyReservations = new MyReservations();
        }
        return sMyReservations;
    }

    public ArrayList<Reservation> getMyReservations() {          // get all Vending Machines
        return mMyReservations;
    }
}
