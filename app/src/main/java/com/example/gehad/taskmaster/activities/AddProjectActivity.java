package com.example.gehad.taskmaster.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.Response;
import com.example.gehad.taskmaster.TaskApplication;
import com.example.gehad.taskmaster.Util;
import com.example.gehad.taskmaster.entities.User;

import java.io.IOException;
import java.util.ArrayList;

public class AddProjectActivity extends Activity {

    private EditText projectNameEditText;
    private EditText projectDescriptionEditText;
    private Button saveButton;

    private static TaskApplication app;

    public static final int ADDED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        app = (TaskApplication) getApplication();

        projectNameEditText = findViewById(R.id.addProject_projectNameEditText);
        projectDescriptionEditText = findViewById(R.id.addProject_projectDescriptionEditText);
        saveButton = findViewById(R.id.addProject_saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Boolean>() {

                    private boolean validInput;
                    private Response response;

                    @Override
                    protected void onPreExecute() {
                        validInput = validateInput();
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        if (!validInput || !aBoolean)
                            return;
                        Intent intent = new Intent();
                        intent.putExtra("project", response.body);
                        setResult(ADDED, intent);
                        finish();
                    }

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        if (!validInput)
                            return false;
                        String servicePath = "/projects/create?token=" + app.getToken();
                        String[] keys = { "name", "description" };
                        Object[] values = { projectNameEditText.getText().toString(), projectDescriptionEditText.getText().toString() };
                        try {
                            response = Util.post(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                            return true;
                        } catch (IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddProjectActivity.this, "Error connecting to the server, try again later...", Toast.LENGTH_LONG).show();
                                }
                            });
                            return false;
                        }
                    }
                }.execute();
            }
        });
    }

    private boolean validateInput() {
        String projectNameString = projectNameEditText.getText().toString();

        boolean good = true;

        if (projectNameString.isEmpty()) {
            projectNameEditText.setError("Name must be provided");
            projectNameEditText.requestFocus();
            good = false;
        }

        return good;
    }
}
