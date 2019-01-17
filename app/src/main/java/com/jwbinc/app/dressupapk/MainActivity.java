package com.jwbinc.app.dressupapk;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

//import com.ramotion.cardslider.CardSliderLayoutManager;
//import com.ramotion.cardslider.CardSnapHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.vistrav.ask.Ask;


public class MainActivity extends AppCompatActivity {

    FirebaseAuth mFireAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    Toolbar mToolbar;
    FirebaseUser mCurrentUser;
    Fragment fragment;
    BottomNavigationViewEx bottomNavigationViewEx;

    private static final int PERMISSION_REQUEST = 100;
    private static final int REQUEST_CODE_FOR_SOURCE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        checkAuthState();

        setContentView(R.layout.activity_main);

        checkpermission();

        fragment = new HomeFragment();
        loadFragment(fragment);


        bottomNavigationViewEx = findViewById(R.id.navigation);
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.home:
                        fragment = new HomeFragment();
                        loadFragment(fragment);
                        break;

                    case R.id.today_dress:
                        fragment = new TodayFragment();
                        loadFragment(fragment);
                        break;

                    case R.id.add_clothes:
                        fragment = new UploadCloth();
                        loadFragment(fragment);
                        break;

                    case R.id.cloth_gallery:
                        fragment = new GalleryFragment();
                        loadFragment(fragment);
                        break;

                }
                return true;
            }
        });

    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFireAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mFireAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }

    private void checkpermission() {
        Ask.on(this)
                .id(PERMISSION_REQUEST) // in case you are invoking multiple time Ask from same activity or fragment
                .forPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withRationales("In other for the app to work perfectly you have to give it permission") //optional
                .go();
    }

    @Override
    public void onBackPressed() {

        if (fragment instanceof TodayFragment) {
            AlertDialog.Builder alertDialogbuilder = new AlertDialog.Builder(this);
            alertDialogbuilder.setTitle("Leave DressUp").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogbuilder.create();
            alertDialog.show();
        } else {
            fragment = new HomeFragment();
            loadFragment(fragment);
        }


    }
}