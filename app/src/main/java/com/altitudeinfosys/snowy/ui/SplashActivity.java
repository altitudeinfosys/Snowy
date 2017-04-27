package com.altitudeinfosys.snowy.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

import altitudeinfosys.com.snowy.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends Activity {

    @Nullable
    @BindView(R.id.imageViewLogo)
    ImageView snowyLogo;

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;


    //progress dialog
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);


        Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        snowyLogo.startAnimation(animation1);
        snowyLogo.startAnimation(animation2);


        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();


        //if getCurrentUser does not returns null
        if(firebaseAuth.getCurrentUser() != null){
            //that means user is already logged in
            //so close this activity
            finish();

            //and open profile activity
            startActivity(new Intent(getApplicationContext(), StartupActivity.class));
        }

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if the objects getcurrentuser method is not null
        //means user is already logged in
        if(firebaseAuth.getCurrentUser() != null){
            //close this activity
            finish();
            //opening profile activity
            startActivity(new Intent(getApplicationContext(), StartupActivity.class));
        }

/*        //initializing views
        editTextEmail = (EditText) findViewById(editTextEmail);
        editTextPassword = (EditText) findViewById(editTextPassword);
        buttonSignIn = (Button) findViewById(R.id.buttonSignin);
        textViewSignup  = (TextView) findViewById(R.id.textViewSignUp);

        progressDialog = new ProgressDialog(this);*/

    }

    public void startSignUp(View view) {
        //calling register method on click
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void login(View view) {
        //calling register method on click
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
