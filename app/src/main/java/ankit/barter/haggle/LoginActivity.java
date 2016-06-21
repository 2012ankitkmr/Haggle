package ankit.barter.haggle;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Bind;

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //Firebase User database get method
        if(isNetworkAvailable()) {
            Firebase.setAndroidContext(this);
            mRootRef = new Firebase("https://haggle-64ac4.firebaseio.com/");
        }
        else
        {
            Toast.makeText(LoginActivity.this, "No Network Service!",
                    Toast.LENGTH_SHORT).show();

        }
        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);


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
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
//Gmail Sign In
        _googleloginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                syncGoogleAccount();
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


        if (!validate()) {
            //onLoginFailed();
            return;
        }

        if(isNetworkAvailable()==true) {
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
                                    onLoginSuccess(email);
                                }
                                else
                                    onLoginFailed();
                                progressDialog.dismiss();

                            } catch (Exception e) {
                                Toast.makeText(getBaseContext(),"User Exception "+e.toString(), Toast.LENGTH_LONG).show();
                                Log.e("User Exception ",e.toString());
                                onLoginFailed();
                                progressDialog.dismiss();
                            }

                        }
                    }, 3000);

        }
        else
        {
            Toast.makeText(LoginActivity.this, "No Network Service!",
                    Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically

                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String str) {


        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Email,str);
        editor.putInt(loginKey,1);
        editor.commit();
        Toast.makeText(getBaseContext(), "Successfully Logged In!", Toast.LENGTH_SHORT).show();
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        //Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_SHORT).show();
        CoordinatorLayout Clayout = (CoordinatorLayout) findViewById(R.id.snackbarlocation);
        Snackbar.make(Clayout, "Username or Password is Incorrect!", Snackbar.LENGTH_SHORT).show();

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

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

//-------------------------------------------------------------------------------------------------------------------------------




    // ---------------------------------------------------Gmail Logic ----------------------------------------------------------------


    private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);

             Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
           String names[] = new String[accounts.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = accounts[i].name;
            }

        return names;
    }

    private AbstractGetNameTask getTask(LoginActivity activity, String email, String scope) {
        return new GetNameInForeground(activity, email, scope);
    }

    public void syncGoogleAccount() {
        if (isNetworkAvailable() == true) {
            String[] accountarrs = getAccountNames();

            if (accountarrs.length > 0) {
                getTask(LoginActivity.this, accountarrs[0], SCOPE).execute();
            } else {
                Toast.makeText(LoginActivity.this, "No Google Account Sync!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LoginActivity.this, "No Network Service!",
                    Toast.LENGTH_SHORT).show();
        }
    }

// ------------------------------------------------------------------------------------------------------------


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
