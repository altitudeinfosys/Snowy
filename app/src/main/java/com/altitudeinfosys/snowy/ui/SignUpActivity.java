package com.altitudeinfosys.snowy.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import altitudeinfosys.com.snowy.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends Activity {
    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    @Nullable
    @BindView(R.id.editTextEmail)
    EditText editTextEmail;

    @Nullable
    @BindView(R.id.editTextPassword) EditText editTextPassword;


    @BindView(R.id.imageViewLogo)
    ImageView snowyLogo;


    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        ButterKnife.bind(this);



        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        snowyLogo.startAnimation(animation1);
        snowyLogo.startAnimation(animation2);
        //initializing firebase auth object


        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        //if getCurrentUser does not returns null
        if(firebaseAuth.getCurrentUser() != null){
            //that means user is already logged in
            //so close this activity
            finish();

            //and open profile activity
            startActivity(new Intent(getApplicationContext(), StartupActivity.class));
        }
    }

    private void registerUser(){

        //getting email and password from edit texts
        String email = editTextEmail.getText().toString().trim();
        String password  = editTextPassword.getText().toString().trim();

        //checking if email and passwords are empty
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email",Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_LONG).show();
            return;
        }

        if(password.length()<6 ){
            Toast.makeText(this,"Please enter a minimum 6 Characters for password",Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){
                            //display some message here
                            Toast.makeText(SignUpActivity.this,"Successfully registered",Toast.LENGTH_LONG).show();
                            startSnowyStartup();
                        }else{
                            //display some message here
                            Toast.makeText(SignUpActivity.this,"Registration Error",Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    public void startSnowyStartup() {
        Intent intent = new Intent(this, StartupActivity.class);
        /*intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);
        //intent.putExtra("longitude", -95.072718);
        //intent.putExtra("latitude", 31.650455);
        intent.putExtra("location", strLocation);*/
        startActivityForResult(intent, 0);
    }


    public void signUp(View view) {
        //calling register method on click
        registerUser();
    }

    public void goLogin(View view)
    {
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
