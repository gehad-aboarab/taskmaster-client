package com.example.gehad.taskmaster.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.gehad.taskmaster.R;

public class MainActivity extends Activity {
    private Button main_loginButton, main_registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_loginButton = (Button) findViewById(R.id.main_loginButton);
        main_registerButton = (Button) findViewById(R.id.main_registerButton);

        main_loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
            }
        });

        main_registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(register);
            }
        });
    }
}
