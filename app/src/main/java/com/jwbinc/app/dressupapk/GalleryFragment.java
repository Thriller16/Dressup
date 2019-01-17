package com.jwbinc.app.dressupapk;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {

    View mView;
    ListView galleryListView;
    TextView noclothyet;
    public GalleryImageAdapter galleryImageAdapter;
    public List<GalleryImage> galleryImageList = new ArrayList<>();
    DatabaseReference mGalleryDatabase;
    FirebaseAuth mFireAuth;
    FirebaseUser mCurrentUser;
    ProgressDialog mProgressDialog;
    GoogleApiClient mGoogleApiClient;


    public GalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mView = inflater.inflate(R.layout.fragment_gallery, container, false);

        galleryListView = mView.findViewById(R.id.galleryListView);
        noclothyet = mView.findViewById(R.id.noImageYet);

        setHasOptionsMenu(true);


        mProgressDialog = new ProgressDialog(getContext());
        mFireAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFireAuth.getCurrentUser();



        mGalleryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("gallery");

        loadUserGallery();



        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("DressUp!");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setElevation(0);
        super.onActivityCreated(savedInstanceState);
    }


    public void loadUserGallery(){
        mProgressDialog.setMessage("Loading your gallery");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);

        mGalleryDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    galleryListView.setVisibility(View.VISIBLE);
                    noclothyet.setVisibility(View.GONE);

                    galleryImageList = new ArrayList<>();
                    for(DataSnapshot gallerySnapshot: dataSnapshot.getChildren()){
                        String url = gallerySnapshot.child("url").getValue().toString();
                        String clothType = gallerySnapshot.child("clothType").getValue().toString();
                        String timestamp = gallerySnapshot.child("timestamp").getValue().toString();

                        galleryImageList.add(new GalleryImage(clothType, timestamp, url));
                        galleryImageAdapter = new GalleryImageAdapter(getContext(), galleryImageList);

                        galleryListView.setAdapter(galleryImageAdapter);
                        galleryImageAdapter.notifyDataSetChanged();

                        mProgressDialog.dismiss();

//                        Toast.makeText(getContext(), "something happened", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

//                    Toast.makeText(getContext(), "Does not exist", Toast.LENGTH_SHORT).show();
                    galleryListView.setVisibility(View.GONE);
                    noclothyet.setVisibility(View.VISIBLE);
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.other_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout_other){
            mFireAuth.signOut();

            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    getActivity().finish();

                }
            });

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(getResources().getString(R.string.googleauthclient)).
                requestEmail().
                build();


        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .enableAutoManage(getActivity(), this)
                .build();
        mGoogleApiClient.connect();
    }
}