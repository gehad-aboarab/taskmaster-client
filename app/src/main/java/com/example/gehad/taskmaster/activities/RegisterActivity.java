package com.example.gehad.taskmaster.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.TaskApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class RegisterActivity extends Activity {
    private TaskApplication app;
    private EditText register_nameEditText, register_emailEditText, register_passwordEditText;
    private ProgressBar register_progressBar;
    private Button register_registerButton;
    private LinearLayout register_form;
    private TextView register_loginLink;

    private UserRegisterTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        app = (TaskApplication) getApplication();

        // Get the references
        register_nameEditText = (EditText) findViewById(R.id.register_nameEditText);
        register_loginLink = (TextView) findViewById(R.id.register_loginLink);
        register_form = (LinearLayout) findViewById(R.id.register_layout);
        register_emailEditText = (EditText) findViewById(R.id.register_emailEditText);
        register_passwordEditText = (EditText) findViewById(R.id.register_passwordEditText);
        register_progressBar = findViewById(R.id.register_progressBar);
        register_registerButton = (Button) findViewById(R.id.register_registerButton);

        // Set button and link listeners
        register_registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        register_loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch the login activity
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
            }
        });
    }

    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors
        register_emailEditText.setError(null);
        register_passwordEditText.setError(null);

        // Store the entered email and password
        String email = register_emailEditText.getText().toString();
        String password = register_passwordEditText.getText().toString();
        String name = register_nameEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            register_passwordEditText.setError(getString(R.string.registerError_requiredPassword));
            focusView = register_passwordEditText;
            cancel = true;
        }
        else if(!isPasswordValid(password)) {
            register_passwordEditText.setError(getString(R.string.registerError_shortPassword));
            focusView = register_passwordEditText;
            cancel = true;
        }

        // Check for a valid email address
        if (TextUtils.isEmpty(email)) {
            register_emailEditText.setError(getString(R.string.registerError_requiredEmail));
            focusView = register_emailEditText;
            cancel = true;
        }
        else if (!isEmailValid(email)) {
            register_emailEditText.setError(getString(R.string.registerError_invalidEmail));
            focusView = register_emailEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress bar, and kick off a background task to
            // perform the user login attempt
            showProgress(true);
            mAuthTask = new UserRegisterTask(email, password, name);
            mAuthTask.execute((Void) null);
        }

    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // Hide the register form and show the progress bar when the registration is taking place
        // Show the register form and hide the progress bar when the registration is complete
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            register_form.setVisibility(show ? View.GONE : View.VISIBLE);
            register_form.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    register_form.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            register_progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            register_progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    register_progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            register_progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            register_progressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {
        private final String mEmail;
        private final String mPassword;
        private final String mName;
        private String connectionError;

        UserRegisterTask(String email, String password, String name) {
            mEmail = email;
            mPassword = password;
            mName = name;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String user;
            String token;
            try {
                URLConnection connection = new URL(app.getBaseUrl()+"/register/"+mName+"/"+mEmail+"/"+mPassword).openConnection();
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                user = scanner.nextLine();
                if(user.equals("User already exists"))
                    return false;
                else if(user.equals("Error connecting to the database")){
                    connectionError="Error";
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                connectionError = "Error";
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            // Send to login page if authentication verified, otherwise show error
            if (success) {
                Intent loginPage = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(loginPage);
//                finish();
            } else {
                if(connectionError == null) {
                    register_emailEditText.setError(getString(R.string.registerError_emailExists));
                    register_emailEditText.requestFocus();
                }
                else{
                    register_emailEditText.setError(getString(R.string.connectionError));
                    register_emailEditText.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
