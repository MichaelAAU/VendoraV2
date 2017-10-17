package com.aaufolks.android.vendora.Controller_Classes;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.aaufolks.android.vendora.R;

/**
 * Created by michalisgratsias on 26/10/16.
 */

public abstract class ParentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();               // abstract method not implemented here

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.fragment_product;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());                       // the view is inflated from xml layout
        FragmentManager fm = getSupportFragmentManager();       // responsible for managing Fragments and adding their Views
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);   // give fragment to Fragment manager
        if (fragment == null) {                                 // maybe this ID is saved on device rotation by fr.mgr, and is not null
            fragment = createFragment();                        // abstract method called to create Fragment
            fm.beginTransaction()                               // create a new FT (Fragment Transaction Object)
                    .add(R.id.fragment_container, fragment)     // include an ADD operation on it (identified by resource ID of container view)
                    .commit(); }                                // and commit the fragment transaction to the list of the mgr
    }

}
