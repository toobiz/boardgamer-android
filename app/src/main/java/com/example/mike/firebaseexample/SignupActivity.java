package com.example.mike.firebaseexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import android.util.Log;

/**
 * Created by mike on 30.11.2016.
 */

public class SignupActivity extends AppCompatActivity {

    private Button buttonSave;
    private Button buttonSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSignin = (Button) findViewById(R.id.sign_in_button);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("myTag", "Signup!");

                Intent profileIntent = new Intent(SignupActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }



}