package activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.celeste.fitnessapp.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sdsmdg.tastytoast.TastyToast;


public class ActivityLogin extends AppCompatActivity {
    private final int RC_SIGN_IN = 123;
    Button btnLogin;
    CallbackManager callbackManager = CallbackManager.Factory.create();
    ProgressBar loadingProgressBar;
    TextInputLayout inputLayoutEmail, inputLayoutPassword;
    FirebaseAuth.AuthStateListener mAuthListener;
    private TextView register, forgotPassword;
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private String TAG = "TAG";
    private GoogleSignInClient mGoogleSignInClient;
    private LoginButton login_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //for changing status bar icon colors
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(ActivityLogin.this, ActivityHome.class));
                } else {
                    // do something
                }
            }
        };
        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(getApplicationContext());
        initComponent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        createRequest();
    }

    private void initComponent() {
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        register = findViewById(R.id.register);
        btnLogin = findViewById(R.id.btnLoginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        inputLayoutEmail = findViewById(R.id.textInputEmail);
        inputLayoutPassword = findViewById(R.id.textInputPassword);
        login_button = findViewById(R.id.login_button);
        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                TastyToast.makeText(getApplicationContext(), "Comming soon ...", TastyToast.LENGTH_LONG, TastyToast.DEFAULT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this, ActivityRegister.class));
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser();
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this, ActivityForgotPassword.class));
            }
        });
    }

    private void signInUser() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (email.isEmpty()) {
            inputLayoutEmail.setError("Email is required");
            inputLayoutEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutEmail.setError("Please enter a valid email address");
        }
        if (password.isEmpty()) {
            inputLayoutPassword.setError("Password is required");
            inputLayoutPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            inputLayoutPassword.setError("Minimum length of password should be at least 6");
            inputLayoutPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(etEmail.getText().toString().trim(), etPassword.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // TastyToast.makeText(ActivityLogin.this, "Signed in", TastyToast.LENGTH_LONG, TastyToast.CONFUSING);
                    signIn();
                    loadingProgressBar.setVisibility(View.GONE);
                } else {
                    TastyToast.makeText(ActivityLogin.this, "" + task.getException().getMessage(), TastyToast.LENGTH_LONG, TastyToast.CONFUSING);
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }
        });


    }

    private void handleFacebookToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookToken: " + accessToken);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "sign in successful: " + task);
                    Toast.makeText(getApplicationContext(), "sign in successful", Toast.LENGTH_LONG).show();
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    TastyToast.makeText(getApplicationContext(), "Comming soon ...", TastyToast.LENGTH_LONG, TastyToast.DEFAULT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(getApplicationContext(), "" + user.getDisplayName(), Toast.LENGTH_LONG).show();
        }
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
    }

    private void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
        Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                Toast.makeText(getApplicationContext(), "ID" + account.getId(), Toast.LENGTH_LONG).show();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {

                // Google Sign In failed, update UI appropriately
                Toast.makeText(getApplicationContext(), "EXCEPTION " + e.getCause(), Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(ActivityLogin.this, ActivityHome.class));
                            overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authorization failed", Toast.LENGTH_LONG).show();

                        }

                        // ...
                    }
                });
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(ActivityLogin.this, ActivityRegister.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}