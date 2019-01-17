package com.jwbinc.app.dressupapk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
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
import com.kwabenaberko.openweathermaplib.Lang;
import com.kwabenaberko.openweathermaplib.Units;
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper;
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.internal.TwitterApi;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import safety.com.br.android_shake_detector.core.ShakeCallback;
import safety.com.br.android_shake_detector.core.ShakeDetector;
import safety.com.br.android_shake_detector.core.ShakeOptions;

public class TodayFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{

    OpenWeatherMapHelper helper = new OpenWeatherMapHelper();
    ImageView weatherIcon;
    TextView weatherMain;
    TextView topText, bottomText;
    ShakeDetector shakeDetector;
    DatabaseReference mSuggestionsDatabase, mGalleryDatabase;
    FirebaseAuth mFireAuth;
    FirebaseUser mCurrentUser;

    List<Suggestion> topSuggestionList = new ArrayList<>();
    List<Suggestion> bottomSuggestionList = new ArrayList<>();

    CardStackLayoutManager topManager, bottomManager;
    SuggestionAdapter topAdapter, bottomAdapter;
    CardStackView topCardStackView, bottomCardStackView;
    String topOrBottom = "";
    String finalWeather = "";
    GoogleApiClient mGoogleApiClient;

    ProgressDialog mProgressDialog;

    public TodayFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View mView = inflater.inflate(R.layout.fragment_today, container, false);
        setHasOptionsMenu(true);

        mFireAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFireAuth.getCurrentUser();

        mProgressDialog = new ProgressDialog(getContext());

        if(mCurrentUser != null){

            mSuggestionsDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("collection");
            mGalleryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("gallery");

            weatherMain = mView.findViewById(R.id.weatherMain);
            weatherIcon = mView.findViewById(R.id.weatherIcon);
            topCardStackView = mView.findViewById(R.id.topCardStackView);
            bottomCardStackView = mView.findViewById(R.id.bottomCardStackView);
            topText = mView.findViewById(R.id.topText);
            bottomText = mView.findViewById(R.id.bottomText);

            getWeather();
        }

        ShakeOptions shakeOptions = new ShakeOptions().background(false).interval(1000).shakeCount(1).sensibility(2.0f);
        this.shakeDetector = new ShakeDetector(shakeOptions).start(getContext(), new ShakeCallback() {
            @Override
            public void onShake() {
                bottomCardStackView.swipe();
                topCardStackView.swipe();
            }
        });


        return mView;

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
        textView.setPadding(150, 0, 0, 0);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(textView);
//        actionBar.setBackgroundDrawable(getResources().getDrawable(R.layout.center_action_bar));

        super.onActivityCreated(savedInstanceState);
    }

    public void getWeather() {
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setMessage("Loading suggestions for today");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        topSuggestionList = new ArrayList<>();
        bottomSuggestionList = new ArrayList<>();

        helper.setApiKey(getResources().getString(R.string.openweather_api_key));
        helper.setUnits(Units.METRIC);
        helper.setLang(Lang.ENGLISH);


        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final double longitude = location.getLongitude();
        double latitude = location.getLatitude();


        helper.getCurrentWeatherByGeoCoordinates(latitude, longitude, new OpenWeatherMapHelper.CurrentWeatherCallback() {
            @Override
            public void onSuccess(CurrentWeather currentWeather) {

                Picasso.get().load("http://openweathermap.org/img/w/" + currentWeather.getWeatherArray().get(0).getIcon() + ".png").into(weatherIcon);
                weatherMain.setText(currentWeather.getWeatherArray().get(0).getMain());

                String weatherDesc = currentWeather.getWeatherArray().get(0).getDescription().replace(" ", "");



                if (weatherDesc.equals("mist") || weatherDesc.contains("clou")) {
                    finalWeather = Weather.cloudy;
                }

                else if (weatherDesc.contains("sky")  || weatherDesc.equals("haz")) {
                    finalWeather = Weather.sunny;
                }

                else if (weatherDesc.contains("rai") || weatherDesc.contains("thu")) {
                    finalWeather = Weather.rainy;
                }


                else if (weatherDesc.equals("snow")) {
                    finalWeather = Weather.snowy;
                }

                else{
                    Toast.makeText(getContext(), currentWeather.getWeatherArray().get(0).getDescription() + "This weather was not included by the developer", Toast.LENGTH_SHORT).show();
                }

                topText.setText("No more tops for a " + finalWeather +  " weather in your collection go to 'Upload' to add more");
                bottomText.setText("No more bottoms for a " + finalWeather +  " weather in your collection go to 'Upload' to add more");

                suggestCloth(finalWeather);

            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.i("TAG", throwable.toString());
            }
        });
    }

    public void suggestCloth(final String weather) {

        topSuggestionList = new ArrayList<>();
        bottomSuggestionList = new ArrayList<>();


        mGalleryDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot gallerySnapshot : dataSnapshot.getChildren()) {

//                        Get the cloth type
                        String typeOfCloth = gallerySnapshot.child("clothType").getValue().toString();
                        if (typeOfCloth.equals(ClothType.t_shirts) || typeOfCloth.equals(ClothType.shirts) || typeOfCloth.equals(ClothType.coats) || typeOfCloth.equals(ClothType.jackets)) {
                            topOrBottom = "top";
                        } else if (typeOfCloth.equals(ClothType.trousers) || typeOfCloth.equals(ClothType.shorts)) {
                            topOrBottom = "bottom";
                        } else if (typeOfCloth.equals(ClothType.gowns)) {
                            Toast.makeText(getContext(), "Is gown so bring up shoes as bottom", Toast.LENGTH_SHORT).show();
                        }

//                        Get the weathers for wearing the cloth
                        String forCloudy = gallerySnapshot.child("forCloudy").getValue().toString();
                        String forRainy = gallerySnapshot.child("forRainy").getValue().toString();
                        String forSnowy = gallerySnapshot.child("forSnowy").getValue().toString();
                        String forSunny = gallerySnapshot.child("forSunny").getValue().toString();


                        String url = gallerySnapshot.child("url").getValue().toString();

                        if (weather.equals(Weather.cloudy) && forCloudy.equals("yes")) {
                            if (topOrBottom.equals("top")) {
                                topSuggestionList.add(new Suggestion(url, "Dress"));
                            } else if (topOrBottom.equals("bottom")) {
                                bottomSuggestionList.add(new Suggestion(url, "Dress"));
                            }
                        }

                        if (weather.equals(Weather.rainy) && forRainy.equals("yes")) {
                            if (topOrBottom.equals("top")) {
                                topSuggestionList.add(new Suggestion(url, "Dress"));
                            } else if (topOrBottom.equals("bottom")) {
                                bottomSuggestionList.add(new Suggestion(url, "Dress"));
                            }
                        }

                        if (weather.equals(Weather.snowy) && forSnowy.equals("yes")) {
                            if (topOrBottom.equals("top")) {
                                topSuggestionList.add(new Suggestion(url, "Dress"));
                            } else if (topOrBottom.equals("bottom")) {
                                bottomSuggestionList.add(new Suggestion(url, "Dress"));
                            }
                        }

                        if (weather.equals(Weather.sunny) && forSunny.equals("yes")) {
                            if (topOrBottom.equals("top")) {
                                topSuggestionList.add(new Suggestion(url, "Dress"));
                            } else if (topOrBottom.equals("bottom")) {
                                bottomSuggestionList.add(new Suggestion(url, "Dress"));
                            }
                        }

                    }


                    setUpManagers();


                    mProgressDialog.dismiss();


                    Collections.shuffle(topSuggestionList);
                    Collections.shuffle(bottomSuggestionList);


                    topAdapter = new SuggestionAdapter(topSuggestionList, getContext());
                    bottomAdapter = new SuggestionAdapter(bottomSuggestionList, getContext());


                    topCardStackView.setLayoutManager(topManager);
                    bottomCardStackView.setLayoutManager(bottomManager);


                    topCardStackView.setAdapter(topAdapter);
                    bottomCardStackView.setAdapter(bottomAdapter);


                } else {
                    mProgressDialog.dismiss();

                    topText.setText("No tops for a " + finalWeather +  " weather in your collection go to 'Add collection' to add more");
                    bottomText.setText("No bottoms for a " + finalWeather +  " weather in your collection go to 'Add collection' to add more");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setUpManagers() {
        topManager = new CardStackLayoutManager(getContext());
        topManager.setStackFrom(StackFrom.None);
        topManager.setVisibleCount(3);
        topManager.setTranslationInterval(8.0f);
        topManager.setScaleInterval(0.95f);
        topManager.setSwipeThreshold(0.3f);
        topManager.setMaxDegree(20.0f);
        topManager.setDirections(Direction.HORIZONTAL);
        topManager.setCanScrollHorizontal(true);
        topManager.setCanScrollVertical(true);


        bottomManager = new CardStackLayoutManager(getContext());
        bottomManager.setStackFrom(StackFrom.None);
        bottomManager.setVisibleCount(3);
        bottomManager.setTranslationInterval(8.0f);
        bottomManager.setScaleInterval(0.95f);
        bottomManager.setSwipeThreshold(0.3f);
        bottomManager.setMaxDegree(20.0f);
        bottomManager.setDirections(Direction.HORIZONTAL);
        bottomManager.setCanScrollHorizontal(true);
        bottomManager.setCanScrollVertical(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }

        shakeDetector.destroy(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_manu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            getWeather();
        }
        if(item.getItemId() == R.id.logout){
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}