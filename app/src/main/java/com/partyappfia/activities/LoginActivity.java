package com.partyappfia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.partyappfia.R;
import com.partyappfia.application.FiaApplication;

public class LoginActivity extends AppCompatActivity {

    private EditText evUsername;
    private EditText evPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        evUsername  = (EditText) findViewById(R.id.ev_username);
        evPassword  = (EditText) findViewById(R.id.ev_password);

        findViewById(R.id.lv_signin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        findViewById(R.id.tv_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void signup() {
        startActivity(new Intent(this, SignupActivity.class));
        overridePendingTransition(R.anim.move_in_left, R.anim.move_out_left);
        finish();
    }

    private void signIn() {
        final String username = evUsername.getText().toString();
        final String password = evPassword.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter user name", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        FiaApplication.getApp().signInWithReadingData(this, username, password);
    }
}
