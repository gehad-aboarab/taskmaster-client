package com.example.gehad.taskmaster.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;

import android.app.Activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.Response;
import com.example.gehad.taskmaster.TaskApplication;
import com.example.gehad.taskmaster.Util;
import com.example.gehad.taskmaster.entities.User;
import com.example.gehad.taskmaster.fragments.HomeActivity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class LoginActivity extends Activity {
    TaskApplication app;
    private UserLoginTask mAuthTask = null;

    private EditText login_emailEditText;
    private EditText login_passwordEditText;
    private ProgressBar login_progressBar;
    private Button login_loginButton;
    private LinearLayout login_form;
    private TextView login_registerLink;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        app = (TaskApplication) getApplication();

        // Get references
        login_registerLink = (TextView) findViewById(R.id.login_registerLink);
        login_form = (LinearLayout) findViewById(R.id.login_layout);
        login_emailEditText = (EditText) findViewById(R.id.login_emailEditText);
        login_passwordEditText = (EditText) findViewById(R.id.login_passwordEditText);
        login_progressBar = findViewById(R.id.login_progressBar);

        login_loginButton = (Button) findViewById(R.id.login_loginButton);

        // Set button and link listeners
        login_loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        login_registerLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the register activity
                Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(register);
            }
        });
    }

    private void startApp() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors
        login_emailEditText.setError(null);
        login_passwordEditText.setError(null);

        // Store email and password values
          email = login_emailEditText.getText().toString();
          password = login_passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check if the user entered a password
        if (TextUtils.isEmpty(password)) {
            login_passwordEditText.setError(getString(R.string.loginError_requiredPassword));
            focusView = login_passwordEditText;
            cancel = true;
        }

        // Check if the user entered an email
        if (TextUtils.isEmpty(email)) {
            login_emailEditText.setError(getString(R.string.loginError_requiredEmail));
            focusView = login_emailEditText;
            cancel = true;
        }

        // Check if the email entered is a valid one
        else if (!isEmailValid(email)) {
            login_emailEditText.setError(getString(R.string.loginError_invalidEmail));
            focusView = login_emailEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress bar, and kick off a background task to
            // perform the user login
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // Hide the login form and show the progress bar when the login is taking place
        // Show the login form and hide the progress bar when the login is complete
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            login_form.setVisibility(show ? View.GONE : View.VISIBLE);
            login_form.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    login_form.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            login_progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            login_progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    login_progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            login_progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            login_form.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mEmail;
        private final String mPassword;
        private Response response;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String servicePath = "/access";
            try {
                String[] keys = {"email", "password"};
                Object[] values = {mEmail, mPassword};
                response = Util.post(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                if (response.status != HttpURLConnection.HTTP_OK)
                    return false;

                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(response.body).getAsJsonObject();
                User loggedUser = gson.fromJson(object.get("user"), User.class);
                String token = gson.fromJson(object.get("token"), String.class);
                app.setLoggedUser(loggedUser);
                app.setToken(token);
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            // Access system if authentication verified, otherwise show error
            if (success) {
                startApp();
            } else {
                if (response == null) {
                    Toast.makeText(LoginActivity.this, "Error connecting to the server", Toast.LENGTH_LONG).show();
                } else if (response.status == HttpURLConnection.HTTP_FORBIDDEN) {
                    login_passwordEditText.setError(getString(R.string.loginError_incorrectCredentials));
                    login_passwordEditText.requestFocus();
                } else {
                    login_passwordEditText.setError(getString(R.string.connectionError));
                    login_passwordEditText.requestFocus();
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


