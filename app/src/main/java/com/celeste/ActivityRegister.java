package com.celeste;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.celeste.fitnessapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.sdsmdg.tastytoast.TastyToast;

public class ActivityRegister extends AppCompatActivity {

    ProgressBar loadingProgressBar;
    EditText etName, etEmail, etMobNumber, etPassword;
    Button btnSignUp;
    TextInputLayout inputLayoutEmail, inputLayoutPassword;
    TextView login;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        changeStatusBarColor();
        initComponent();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initComponent() {
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        etMobNumber = findViewById(R.id.editTextMobile);
        etName = findViewById(R.id.editTextName);
        btnSignUp = findViewById(R.id.btnRegister);
        inputLayoutEmail = findViewById(R.id.textInputEmail);
        inputLayoutPassword = findViewById(R.id.textInputPassword);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }


    private void registerUser() {
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
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            TastyToast.makeText(ActivityRegister.this, "Authentication successful", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                            startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
                            loadingProgressBar.setVisibility(View.GONE);
                            //  updateUI(user);
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                TastyToast.makeText(ActivityRegister.this, "A user with this email address already exists", TastyToast.LENGTH_LONG,
                                        TastyToast.INFO);
                            } else {
                                TastyToast.makeText(ActivityRegister.this, "" + task.getException(), TastyToast.LENGTH_LONG,
                                        TastyToast.INFO);
                            }
                            loadingProgressBar.setVisibility(View.GONE);
                            // updateUI(null);
                        }
                    }
                });
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);

    }
}