package com.example.foodorder.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.foodorder.R;
import com.example.foodorder.common.Common;
import com.example.foodorder.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    MaterialEditText edtPhone, edtPassword;
    Button btnSignIn;
    CheckBox cbRemember;
    public static User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPhone = (MaterialEditText) findViewById(R.id.editPhone);
        edtPassword = (MaterialEditText) findViewById(R.id.editPassword);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        cbRemember = (CheckBox) findViewById(R.id.check_remember);

        //init paper
        Paper.init(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignIn.setOnClickListener((view) -> {

            if (Common.isConnectedToInternet(getBaseContext())) {

                if (cbRemember.isChecked()) {
                    Paper.book().write(Common.USER_KEY, edtPhone.getText().toString());
                    Paper.book().write(Common.PWD_KEY, edtPassword.getText().toString());
                }

                final ProgressDialog mdDialog = new ProgressDialog(SignIn.this);
//                mdDialog.setMessage("Please waiting....");
//                mdDialog.show();

                table_user.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.child(edtPhone.getText().toString()).exists()) {

                            User user = snapshot.child(edtPhone.getText().toString()).getValue(User.class);
                            user.setPhone(edtPhone.getText().toString());
                            if (user.getPassword().equals(edtPassword.getText().toString())) {
//                                Intent homeIntent = new Intent(SignIn.this, MainActivity.class);
                                Common.currentUser = user;
                                Handler handler = new Handler();
                                handler.postDelayed(() -> {
                                    Intent intent = new Intent(SignIn.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                    mdDialog.dismiss();
                                }, 2000);
                                mdDialog.setMessage("Please waiting....");
                                mdDialog.show();
//                                startActivity(homeIntent);
//                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Sign in failed !", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "User not exist in Database!", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } else {
                Toast.makeText(SignIn.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                return;
            }


                }
        );
    }
}