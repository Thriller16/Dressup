package com.jwbinc.app.dressupapk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class RegisterActivity extends AppCompatActivity  implements GoogleApiClient.OnConnectionFailedListener {

    EditText emailEdt, passwEdt;
    TextView goToLogin;
    Button registerButton;
    FirebaseAuth mFireAuth;
    DatabaseReference mUserDatabase;
    FirebaseUser mCurrentUser;
    ProgressDialog mProgressDialog;
    SignInButton googleSignInBtn;
    TwitterLoginButton twitterSignInButton;
    CallbackManager callbackManager;
    GoogleApiClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getResources().getString(R.string.twiiter_consumer_api), getResources().getString(R.string.twitter_api_secret));
        Fabric.with(this, new TwitterCore(authConfig));


        setContentView(R.layout.activity_register);
        callbackManager = CallbackManager.Factory.create();


        registerButton = findViewById(R.id.reg_btn);
        emailEdt = findViewById(R.id.email_reg);
        googleSignInBtn = findViewById(R.id.googleButton_reg);
        twitterSignInButton = findViewById(R.id.twitterButton_reg);
        goToLogin = findViewById(R.id.go_to_login);
        passwEdt = findViewById(R.id.passw_reg);
        mFireAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgressDialog = new ProgressDialog(this);

        getSupportActionBar().hide();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailEdt.getText().toString() == null || emailEdt.getText().toString() == "" || !emailEdt.getText().toString().contains("@") || !emailEdt.getText().toString().contains(".com")) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                } else if (passwEdt.getText().toString() == null || passwEdt.getText().toString() == "" || passwEdt.getText().toString().length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Please enter a password longer than 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(emailEdt.getText().toString(), passwEdt.getText().toString());
                }
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
//                If this does not work then we just call finish
            }
        });

        setGoogleButton(googleSignInBtn);


        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUserInWithGoogle();
            }
        });

        twitterSignInButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException e) {
//                Toast.makeText(LoginActivity.this, "" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
//                Toast.makeText(LoginActivity.this, "" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void registerUser(String email, String password) {

        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setMessage("Your account is being created");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        final HashMap<Object, Object> userMap = new HashMap<>();
        userMap.put("isSharingCollection", "no");
        userMap.put("collection", "");
        mFireAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mCurrentUser = mFireAuth.getCurrentUser();

                if (task.isSuccessful()) {

                    mUserDatabase.child(mCurrentUser.getUid()).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Your account has been created", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                } else {

                }
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mFireAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void handleTwitterSession(TwitterSession session) {
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setMessage("Logging In");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);


        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mFireAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    mProgressDialog.dismiss();
                }
                else{
                    Toast.makeText(RegisterActivity.this, "task not successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signUserInWithGoogle(){
        Intent signInintent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInintent, RC_SIGN_IN);
    }

    protected void setGoogleButton(SignInButton signInButton){
        for(int i = 0; i < signInButton.getChildCount(); i ++){
            View v = signInButton.getChildAt(i);

            if(v instanceof  TextView){
                TextView tv  = (TextView)v;

                tv.setText("Log in with Google");
                tv.setPadding(0, 0, 0, 0);
                return;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                requestIdToken(getResources().getString(R.string.googleauthclient)).
                requestEmail().
                build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void handleGoogleSession(GoogleSignInAccount account) {
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setMessage("Logging In");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);



        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFireAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    mProgressDialog.dismiss();
                }
                else {
                    Toast.makeText(RegisterActivity.this, "task was not successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleGoogleSession(account);

            }catch (ApiException e) {
                Toast.makeText(this, "No account was selected", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        else if (TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE == requestCode){
            twitterSignInButton.onActivityResult(requestCode, resultCode, data);
        }



        else {
//            Toast.makeText(this, "result code for sign in is null", Toast.LENGTH_SHORT).show();
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

}