package com.jwbinc.app.dressupapk;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kwabenaberko.openweathermaplib.Lang;
import com.kwabenaberko.openweathermaplib.Units;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadCloth extends Fragment {

    private static final int GALLERY_PICK = 1;
    private static final int CAMERA_REQUEST = 1888;
    String[] allDressTypes = {ClothType.t_shirts, ClothType.shirts, ClothType.trousers, ClothType.shorts, ClothType.gowns, ClothType.coats, ClothType.jackets, ClothType.shoes};
    Spinner mClothTypeSpinner;
    ImageButton pickFromGalleryBtn, takeWithCameraBtn;
    Button addToCollectionBtn;
    View mView;
    ImageView selectedCloth;
    FirebaseAuth mFireAuth;
    FirebaseUser mCurrentUser;
    DatabaseReference mCollectionDatabase;
    DatabaseReference mGalleryDatabase;
    GoogleApiClient mGoogleApiClient;

    StorageReference mCollectionsStorage;
    Uri finalImageUri;
    ProgressDialog mProgressDialog;
    String finalWeather, finalDressType, imageUid;
//    int counter = 0;

    String sunny, cloudy, rainy, snowy = "" ;
    RadioButton sunnyBox, rainyBox, cloudyCheckBox, snowyCheckbox;


    public UploadCloth() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setUpViews(inflater, container);

        mProgressDialog = new ProgressDialog(getContext());

        mFireAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFireAuth.getCurrentUser();
        mCollectionDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("collection");
        mGalleryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("gallery");
        mCollectionsStorage = FirebaseStorage.getInstance().getReference();


        setHasOptionsMenu(true);
        ArrayAdapter<String> clothType_arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, allDressTypes);
        clothType_arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mClothTypeSpinner.setAdapter(clothType_arrayAdapter);
        mClothTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                finalDressType = allDressTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        takeWithCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImageWithCamera();
//                Toast.makeText(getContext(), "This button has been disabled by the developer", Toast.LENGTH_SHORT).show();
            }
        });

        pickFromGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });

        addToCollectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        return mView;
    }


    public void peformUploadProcess(final String dressType, final String forSunny, final String forCloudy, final String forRainy, final String forSnowy)
    {

//        After the dressType the next child is meant to be mood
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setMessage("Your file is being uploaded");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        if (finalImageUri == null) {
            Toast.makeText(getContext(), "Cannot upload empty Uri into firebase storage", Toast.LENGTH_SHORT).show();
        } else {

            final String uidforimage = mCollectionDatabase.child(dressType).push().getKey();

            mCollectionsStorage.child(mCurrentUser.getUid()).child("collection").child(dressType).child(uidforimage + ".jpg")
                    .putFile(finalImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        final String downloadUrl = task.getResult().getDownloadUrl().toString();

                        final HashMap<Object, String> uploadedFileMap = new HashMap<>();
                        uploadedFileMap.put("url", downloadUrl);
                        uploadedFileMap.put("clothType", dressType);
                        uploadedFileMap.put("timestamp", getDateTime());
                        uploadedFileMap.put("forSunny", forSunny);
                        uploadedFileMap.put("forCloudy", forCloudy);
                        uploadedFileMap.put("forRainy", forRainy);
                        uploadedFileMap.put("forSnowy", forSnowy);

                        mGalleryDatabase.child(uidforimage).setValue(uploadedFileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(getContext(), "Could not upload file", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                }
            });
        }

    }

    private void uploadImage() {

        sunny = "no";
        rainy = "no";
        cloudy = "no";
        snowy = "no";

        if (sunnyBox.isChecked()) {
            sunny = "yes";
        }

        if (rainyBox.isChecked()) {
            rainy = "yes";
        }

        if (cloudyCheckBox.isChecked()) {
            cloudy = "yes";
        }

        if (snowyCheckbox.isChecked()) {
            snowy = "yes";
        }

        peformUploadProcess(finalDressType, sunny, cloudy, rainy, snowy);
    }

    private void selectImageFromGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Image to Upload"), GALLERY_PICK);
    }

    public void captureImageWithCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            finalImageUri = selectedImage;



            Picasso.get().load(selectedImage).noPlaceholder().centerCrop().fit().into(selectedCloth);
        }

        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//
            Bitmap cameraImage = (Bitmap) data.getExtras().get("data");

            finalImageUri = getImageUri(getContext(), cameraImage);

            Picasso.get().load(getImageUri(getContext(), cameraImage)).noPlaceholder().centerCrop().fit().into(selectedCloth);

        }
    }

    public void setUpViews(LayoutInflater inflater, ViewGroup container) {
        mView = inflater.inflate(R.layout.fragment_upload_cloth, container, false);


        mClothTypeSpinner = mView.findViewById(R.id.clothTypeSpinner);
        selectedCloth = mView.findViewById(R.id.selectedImageView);
        pickFromGalleryBtn = mView.findViewById(R.id.pickFromGallery);
        takeWithCameraBtn = mView.findViewById(R.id.takeWithCamera);
        sunnyBox = mView.findViewById(R.id.sunnyCheckBox);
        rainyBox = mView.findViewById(R.id.rainyCheckBox);
        cloudyCheckBox = mView.findViewById(R.id.cloudycheckBox);
        snowyCheckbox = mView.findViewById(R.id.snowyCheckbox);
        takeWithCameraBtn = mView.findViewById(R.id.takeWithCamera);
        addToCollectionBtn = mView.findViewById(R.id.addToCollection);
    }

    private String getDateTime() {
        String dateTime;

        Date currentTime = Calendar.getInstance().getTime();
        long date = currentTime.getDate();
        long month = currentTime.getMonth();
        long year = currentTime.getYear();
        int hours = currentTime.getHours();
        int minutes = currentTime.getMinutes();

        String finalYear = String.valueOf(year).substring(String.valueOf(year).length() - 2);


        dateTime = date + "/" + month + "/20" + finalYear + " " + hours + ":" + minutes;


        return dateTime;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
//        actionBar.setTitle("DressUp!");
        actionBar.setElevation(0);
        TextView textView = new TextView(getContext());
        textView.setText("DressUp!");
        textView.setTextSize(23);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.white));
        textView.setPadding(40, 0, 0, 0);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(textView);
//        actionBar.setBackgroundDrawable(getResources().getDrawable(R.layout.center_action_bar));

        super.onActivityCreated(savedInstanceState);
    }
}