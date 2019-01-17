package com.jwbinc.app.dressupapk;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.Auth;
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
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    EditText emailEdt, passwEdt;
    TextView goToReg;
    FirebaseAuth mFireAuth;
    Button loginBtn;
    ProgressDialog mProgressDialog;
    GoogleApiClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    SignInButton googleSignInBtn;
    TwitterLoginButton twitterSignInButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getResources().getString(R.string.twiiter_consumer_api), getResources().getString(R.string.twitter_api_secret));
        Fabric.with(this, new TwitterCore(authConfig));


        setContentView(R.layout.activity_login);
        callbackManager = CallbackManager.Factory.create();


        emailEdt = findViewById(R.id.email_login);
        passwEdt = findViewById(R.id.passw_login);
        googleSignInBtn = findViewById(R.id.googleButton);
        twitterSignInButton = findViewById(R.id.twitterButton);
        mProgressDialog = new ProgressDialog(this);
        loginBtn = findViewById(R.id.login_btn);
        goToReg = findViewById(R.id.go_to_reg);
        mFireAuth = FirebaseAuth.getInstance();

        getSupportActionBar().hide();

        goToReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        setGoogleButton(googleSignInBtn);


        if(isConnectingToInternet(this)){

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

            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    singInUserWithEmail(emailEdt.getText().toString(), passwEdt.getText().toString());
                    if (emailEdt.getText().toString() == null || emailEdt.getText().toString() == "" || !emailEdt.getText().toString().contains("@") || !emailEdt.getText().toString().contains(".com")) {
                        Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    } else if (passwEdt.getText().toString() == null || passwEdt.getText().toString() == "" || passwEdt.getText().toString().length() < 6) {
                        Toast.makeText(LoginActivity.this, "Please input password correctly", Toast.LENGTH_SHORT).show();
                    } else {
                        singInUserWithEmail(emailEdt.getText().toString(), passwEdt.getText().toString());
                    }
                }
            });

        }
        else{
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mFireAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
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
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    mProgressDialog.dismiss();
                }
                else{
                    Toast.makeText(LoginActivity.this, "task not successful", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void signUserInWithGoogle(){
        Intent signInintent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInintent, RC_SIGN_IN);
    }

    private void singInUserWithEmail(String email, String password) {
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setMessage("Logging In");
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);


        mFireAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    mProgressDialog.dismiss();
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
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    mProgressDialog.dismiss();
                }
                else {
                    Toast.makeText(LoginActivity.this, "task was not successful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }


}