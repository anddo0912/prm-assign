package com.example.foodorder.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorder.R;
import com.example.foodorder.common.Common;
import com.example.foodorder.model.User;
import com.example.foodorder.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Random;

public class SignUp extends AppCompatActivity {

    MaterialEditText edtPhone, edtPassword, edtName;
    Button btnSignUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        edtPhone = (MaterialEditText) findViewById(R.id.editPhone);
        edtPassword = (MaterialEditText) findViewById(R.id.editPassword);
        edtName = (MaterialEditText) findViewById(R.id.editName);

        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {

                                         @Override
                                         public void onClick(View v) {
                                             if (Common.isConnectedToInternet(getBaseContext())) {

                                                 final ProgressDialog mdDialog = new ProgressDialog(SignUp.this);
                                                 mdDialog.setMessage("Please waiting....");
                                                 mdDialog.show();

                                                 table_user.addValueEventListener(new ValueEventListener() {

                                                     @Override
                                                     public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                         if (snapshot.child(edtPhone.getText().toString()).exists()) {
                                                             mdDialog.dismiss();
                                                             Toast.makeText(getApplicationContext(), "Phone already register !", Toast.LENGTH_SHORT).show();
                                                         } else {
                                                             mdDialog.dismiss();
                                                             final int random = new Random().nextInt(61) + 20;
                                                             User user = new User(edtName.getText().toString(), edtPassword.getText().toString(), edtPhone.getText().toString());
                                                             table_user.child(edtPhone.getText().toString()).setValue(user);
                                                             Toast.makeText(getApplicationContext(), "Sign up successfully !", Toast.LENGTH_SHORT).show();
                                                             finish();
                                                         }

                                                     }

                                                     @Override
                                                     public void onCancelled(@NonNull DatabaseError error) {

                                                     }
                                                 });
                                             } else {
                                                 Toast.makeText(SignUp.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                                                 return;
                                             }
                                         }
                                     }
        );
    }
}