package com.jwbinc.app.dressupapk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.os.Build;

import android.support.v7.widget.RecyclerView;

//import com.github.tbouron.shakedetector.library.ShakeDetector;
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
import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jwbinc.app.dressupapk.cards.SliderAdapter;
import com.jwbinc.app.dressupapk.utils.DecodeBitmapTask;

import java.util.ArrayList;
import java.util.List;

import safety.com.br.android_shake_detector.core.ShakeCallback;
import safety.com.br.android_shake_detector.core.ShakeDetector;
import safety.com.br.android_shake_detector.core.ShakeOptions;

public class HomeFragment extends Fragment {

    List<Suggestion> suggestionList = new ArrayList<>();


    TextView noclothyet;
    private SliderAdapter sliderAdapter;

    private CardSliderLayoutManager layoutManger;
    private RecyclerView recyclerView;
    private int currentPosition;
    FirebaseAuth mFireAuth;
    GoogleApiClient mGoogleApiClient;
    ShakeDetector shakeDetector;
    DatabaseReference mGalleryDatabase;
    FirebaseUser mCurrentUser;
    ProgressDialog mProgressDialog;


    private DecodeBitmapTask decodeMapBitmapTask;
    View mView;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        initRecyclerView(mView);
        new CardSnapHelper().attachToRecyclerView(recyclerView);
        noclothyet = mView.findViewById(R.id.noImageYetHome);

        mFireAuth = FirebaseAuth.getInstance();
        setHasOptionsMenu(true);
////
        mFireAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFireAuth.getCurrentUser();

        if(mCurrentUser != null){
            mProgressDialog = new ProgressDialog(getContext());



            mGalleryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).child("gallery");

        loadUserGallery(mView);


        }

        else{

        }


        ShakeOptions shakeOptions = new ShakeOptions().background(false).interval(1000).shakeCount(1).sensibility(2.0f);
        this.shakeDetector = new ShakeDetector(shakeOptions).start(getContext(), new ShakeCallback() {
            @Override
            public void onShake() {
                recyclerView.smoothScrollBy(300, 0);
            }
        });


        return mView;
    }


    public void loadUserGallery(final View view){
        mProgressDialog.setMessage("Loading your collection");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);

        mGalleryDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
//                    galleryListView.setVisibility(View.VISIBLE);
//                    noclothyet.setVisibility(View.GONE);

                    suggestionList = new ArrayList<>();
                    for(DataSnapshot gallerySnapshot: dataSnapshot.getChildren()){
                        String url = gallerySnapshot.child("url").getValue().toString();


                        suggestionList.add(new Suggestion(url, ""));
                        sliderAdapter = new SliderAdapter(suggestionList.size(), suggestionList, new OnCardClickListener());

                        initRecyclerView(view);
                        mProgressDialog.dismiss();

                    }
                }
                else {
                    recyclerView.setVisibility(View.GONE);
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
        textView.setPadding(30, 0, 0, 0);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(textView);
//        actionBar.setBackgroundDrawable(getResources().getDrawable(R.layout.center_action_bar));

        super.onActivityCreated(savedInstanceState);
    }

    private void initRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(sliderAdapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onActiveCardChange();
                }
            }
        });

        layoutManger = (CardSliderLayoutManager) recyclerView.getLayoutManager();

    }
    @Override
    public void onPause() {
        super.onPause();
//        if (isFinishing() && decodeMapBitmapTask != null) {
//            decodeMapBitmapTask.cancel(true);
//        }
    }

    private void onActiveCardChange() {
        final int pos = layoutManger.getActiveCardPosition();
        if (pos == RecyclerView.NO_POSITION || pos == currentPosition) {
            return;
        }

        onActiveCardChange(pos);
    }

    private void onActiveCardChange(int pos) {
        int animH[] = new int[] {R.anim.slide_in_right, R.anim.slide_out_left};
        int animV[] = new int[] {R.anim.slide_in_top, R.anim.slide_out_bottom};

        final boolean left2right = pos < currentPosition;
        if (left2right) {
            animH[0] = R.anim.slide_in_left;
            animH[1] = R.anim.slide_out_right;

            animV[0] = R.anim.slide_in_bottom;
            animV[1] = R.anim.slide_out_top;
        }
        currentPosition = pos;
    }

    private class OnCardClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final CardSliderLayoutManager lm =  (CardSliderLayoutManager) recyclerView.getLayoutManager();

            if (lm.isSmoothScrolling()) {
                return;
            }

            final int activeCardPosition = lm.getActiveCardPosition();
            if (activeCardPosition == RecyclerView.NO_POSITION) {
                return;
            }

            final int clickedPosition = recyclerView.getChildAdapterPosition(view);
            if (clickedPosition == activeCardPosition) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                    startActivity(intent);
                } else {
                }
            } else if (clickedPosition > activeCardPosition) {
                recyclerView.smoothScrollToPosition(clickedPosition);
                onActiveCardChange(clickedPosition);
            }
        }
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
    public void onResume() {
        super.onResume();
//        ShakeDetector.start();
    }

    @Override
    public void onStop() {
        super.onStop();
//        ShakeDetector.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shakeDetector.destroy(getContext());
    }
}

