package ankit.barter.haggle.Signing;



import android.app.AlertDialog;
import android.app.ProgressDialog;
import com.facebook.FacebookSdk;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Button;
import android.widget.EditText;


import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import ankit.barter.haggle.ClientClasses.CloudinaryClient;
import ankit.barter.haggle.MainActivity;
import ankit.barter.haggle.NoNetworkActivity;
import ankit.barter.haggle.R;
import ankit.barter.haggle.StructureClasses.User;
import butterknife.ButterKnife;
import butterknife.Bind;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;


    private static final int RC_SIGN_IN = 9001;

    @Bind(R.id.input_email)
    EditText _emailText;
    @Bind(R.id.input_password)
    EditText _passwordText;
    @Bind(R.id.btn_login)
    Button _loginButton;
    @Bind(R.id.google_login)
    Button _googleloginButton;
    @Bind(R.id.link_signup)
    TextView _signupLink;

    Context mContext = LoginActivity.this;
    AccountManager mAccountManager;
    String token;
    int serverCode;
    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";


    Firebase mRootRef;
    Map<String, Map<String, String>> Database = null;


    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Email = "EmailKey";
    public static final String loginKey = "loginKey";
    SharedPreferences sharedpreferences;


    private CallbackManager mcallbackManager;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //Firebase User database get method
        if(isNetworkAvailable()) {Intent in = new Intent(LoginActivity.this, NoNetworkActivity.class);
            startActivity(in);
               }
        Firebase.setAndroidContext(this);
        mRootRef = new Firebase("https://haggle-64ac4.firebaseio.com/");

        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
//--------------------------------GOOGLE LOGIN--------------------------------------------------------------------------------------
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


   //----------------------------------------------------------------------------------------------------------/
        ////-------------------FacebookLOGIN--------------------------------------------------------------------------------


        AppEventsLogger.activateApp(this);

        LoginButton _facebookloginButton =(LoginButton)findViewById(R.id.facebook_login);

        _facebookloginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        mcallbackManager = CallbackManager.Factory.create();

_facebookloginButton.registerCallback(mcallbackManager,mCallback);




        //--------------------------------------------------------------------------------------------------------------------

        // Login Button Code
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        //Sign up Button Code
        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });
//Gmail Sign In
        _googleloginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SignInGoogle();
            }
        });


    }

// --------------------------------------------------Basic Login Logic----------------------------------------------------------------

    protected void onStart() {
        super.onStart();
        if(isNetworkAvailable()==true) {
          //  Toast.makeText(LoginActivity.this, "Here", Toast.LENGTH_SHORT).show();
            Firebase UserRef = mRootRef.child("Users");
            UserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Database = dataSnapshot.getValue(Map.class);
                    //Log.e("Present","here");
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
        else
        {
            Toast.makeText(LoginActivity.this, "No Network Service!",
                    Toast.LENGTH_SHORT).show();

        }
    }

    public void login() {
        Log.d(TAG, "Login");
        if(isNetworkAvailable()) {

        if (!validate()) {
            //onLoginFailed();
            return;
        }
            _loginButton.setEnabled(false);


            final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();

            final String email = _emailText.getText().toString().replace(".", "_");
            final String password = _passwordText.getText().toString();

            // TODO: Implement your own authentication logic here.

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // On complete call either onLoginSuccess or onLoginFailed

                            try {
                                Map<String, String> userinfo = Database.get(email);
//                                Toast.makeText(getBaseContext(),"Pass "+ userinfo.get("Password"), Toast.LENGTH_LONG).show();

                                if ((password != null && userinfo != null) && userinfo.get("Password").equals(password)) {
                                 //   ProfileFragment us = new ProfileFragment();
                                   // us.setText(userinfo);
                                    onLoginSuccess(email,1);
                                }
                                else
                                    onLoginFailed();
                                progressDialog.dismiss();

                            } catch (Exception e) {
                               // Toast.makeText(getBaseContext(),"User Exception", Toast.LENGTH_LONG).show();
                                Log.e("User Exception ",e.toString());
                                onLoginFailed();
                                progressDialog.dismiss();
                            }

                        }
                    }, 2000);

        }
        else
        {
            Toast.makeText(LoginActivity.this, "No Network Service!",
                    Toast.LENGTH_SHORT).show();
        }
    }





    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String str,int k) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Email,str);
        editor.putInt(loginKey,k);
        editor.commit();
        Toast.makeText(getBaseContext(), "Successfully Logged In!", Toast.LENGTH_SHORT).show();
        _loginButton.setEnabled(true);
      //  Intent intent = new Intent(LoginActivity.this,MainActivity.class);
     //  startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        //Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_SHORT).show();
        //CoordinatorLayout Clayout = (CoordinatorLayout) findViewById(R.id.snackbarlocation);
       // Snackbar.make(getC, "Username or Password is Incorrect!", Snackbar.LENGTH_SHORT).show();
        _emailText.setError("Email And Password Don't Match");
        _passwordText.setError("Email And Password Don't Match");

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 15) {
            _passwordText.setError("between 4 and 15 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

//-------------------------------------------------------------------------------------------------------------------------------




    // ---------------------------------------------------Gmail Logic ----------------------------------------------------------------
private void SignInGoogle()
{
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);
}
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("Problem", String.valueOf(requestCode));

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else
        {
         mcallbackManager.onActivityResult(requestCode,resultCode,data);
        }
    }


    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

//            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
           final String mail = acct.getEmail().toString().replace(".","_");

            User NewUser  = new User( acct.getEmail().toString() ,  acct.getDisplayName(), "p" , mail ,"(Please Specify)",mail,mail);

            mRootRef.child("Users").child(mail).setValue(NewUser);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Email,mail);
            editor.commit();
            onLoginSuccess(mail,2);

        } else {
            // Signed out, show unauthenticated UI.
  //          updateUI(false);
            Toast.makeText(LoginActivity.this, "Login Failed..!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


// ------------------------------------------------------------------------------------------------------------
//-------------------------Facebook Login-----------------------------------------------------------------
private FacebookCallback<LoginResult> mCallback =new FacebookCallback<LoginResult>() {
    @Override
    public void onSuccess(LoginResult loginResult) {
        Log.e("Problem", "Problem");

        AccessToken accessToken =loginResult.getAccessToken();
        final Profile profile =Profile.getCurrentProfile();
        if(profile!=null)
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
            final EditText edittext = new EditText(LoginActivity.this);
            alert.setMessage("Please Specify Your Email Address!");
            alert.setTitle("Required Info");

            alert.setView(edittext);

            alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String EditTextValue = edittext.getText().toString();
                    String email = EditTextValue;
                    final String mail = email.replace(".","_");
                    Uri uri = profile.getProfilePictureUri(200,200);

                    try {
                        InputStream fileInputStream=getBaseContext().getContentResolver().openInputStream(uri);
                        CloudinaryClient.upload(fileInputStream,mail);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    User NewUser  = new User( email ,  profile.getName(), "p" , mail ,"(Please Specify)",mail,mail);

                    mRootRef.child("Users").child(mail).setValue(NewUser);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Email,mail);
                    editor.commit();
                    onLoginSuccess(mail,2);

                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(LoginActivity.this, "Can't Login!", Toast.LENGTH_SHORT).show();
                }
            });

            alert.show();

        }
        else {
            Toast.makeText(LoginActivity.this, "Login In Facebook App First!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onCancel() {
        Log.e("Problem", "Problem");

    }

    @Override
    public void onError(FacebookException error) {
        Log.e("Problem", "Problem");

        Toast.makeText(LoginActivity.this, "Facebook Auth Error!", Toast.LENGTH_SHORT).show();
    }
};

    //----------------------------------------------------------------------------------------------------------

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.e("Network Testing", "Available");
            return true;
        }

        Log.e("Network Testing", "Not Available");
        return false;
    }



}
