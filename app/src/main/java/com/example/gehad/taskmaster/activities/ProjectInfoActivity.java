package com.example.gehad.taskmaster.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.Response;
import com.example.gehad.taskmaster.TaskApplication;
import com.example.gehad.taskmaster.Util;
import com.example.gehad.taskmaster.entities.Project;
import com.example.gehad.taskmaster.entities.Section;
import com.example.gehad.taskmaster.entities.User;
import com.example.gehad.taskmaster.fragments.HomeActivity;
import com.example.gehad.taskmaster.fragments.ProjectSectionsActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectInfoActivity extends Activity {
    private EditText projectNameEditText;
    private EditText projectDescriptionEditText;
    private ListView projectMembersListView;
    private TaskApplication app;
    private int projectId;
    private Button saveProjectButton, addMembersButton;

    private Project currentProject = null;

    public static final int PROJECT_EDITED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);

        app = (TaskApplication) getApplication();
        projectNameEditText = (EditText) findViewById(R.id.projectInfo_projectName);
        projectDescriptionEditText = (EditText) findViewById(R.id.projectInfo_projectDescription);
        projectMembersListView = (ListView) findViewById(R.id.projectInfo_projectMembersListView);

        addMembersButton = (Button) findViewById(R.id.projectInfo_addMembersButton);
        saveProjectButton = (Button) findViewById(R.id.projectInfo_saveProjectButton);

        saveProjectButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Boolean>() {

                    private Response response;
                    private boolean validInput;

                    @Override
                    protected void onPreExecute() {
                        validInput = validateInput();
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        if (!validInput || !success)
                            return;
                        Intent intent = new Intent();
                        intent.putExtra("projectJson", response.body);
                        setResult(PROJECT_EDITED, intent);
                        finish();
                    }

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        if (!validInput)
                            return false;
                        String servicePath = "/projects/" + projectId
                                + "?token=" + app.getToken();
                        String[] keys = { "name", "description" };
                        Object[] values = { projectNameEditText.getText().toString(), projectDescriptionEditText.getText().toString() };
                        try {
                            response = Util.put(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                            return true;
                        } catch (IOException e) {
                            return false;
                        }
                    }

                }.execute();
            }
        });

        projectId = getIntent().getIntExtra("projectId", 0);
        for (Project p : app.getProjects()) {
            if (p.getProjectId() == projectId) {
                currentProject = p;
            }
        }

        projectNameEditText.setText(currentProject.getName());
        projectDescriptionEditText.setText(currentProject.getDescription());

        populateMembers();

        addMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProjectInfoActivity.this, AddMemberActivity.class);
                intent.putExtra("projectId", projectId);
                startActivity(intent);
            }
        });

        projectMembersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final User selectedUser = (User) adapterView.getItemAtPosition(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(ProjectInfoActivity.this);
                AlertDialog.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == AlertDialog.BUTTON_POSITIVE) {
                            new AsyncTask<Void, Void, Boolean>() {
                                Response response;
                                @Override
                                protected Boolean doInBackground(Void... voids) {
                                    try {
                                        String servicePath = "/projects/" + app.getCurrentProject().getProjectId() + "/members/" + selectedUser.getUserId() + "?token=" + app.getToken();
                                        response = Util.delete(app.getBaseUrl() + servicePath, new HashMap<String, Object>());
                                        if (response.status != 202) {
                                            Log.d("GEHAD_TAG",response.status + ", " + response.body);
                                            return false;
                                        }
                                        return true;
                                    } catch (IOException e) {
                                        return false;
                                    }
                                }

                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    if (aBoolean) {
                                        for(int i=0; i<app.getProjectMembers().size(); ++i){
                                            if(app.getProjectMembers().get(i).getUserId() == selectedUser.getUserId()){
                                                app.getProjectMembers().remove(i);
                                                break;
                                            }
                                        }
                                        populateMembers();
                                        Toast.makeText(ProjectInfoActivity.this, "Member deleted successfully.", Toast.LENGTH_LONG).show();
                                        if(selectedUser.getUserId() == app.getLoggedUser().getUserId()){
                                            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(ProjectInfoActivity.this, "An error occurred. Please try again later.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }.execute();
                        }
                    }
                };

                AlertDialog dialog = builder.setTitle("Delete member")
                        .setMessage("Are you sure you want to delete this member?")
                        .setPositiveButton("Confirm", listener)
                        .setNegativeButton("Cancel", listener)
                        .show();

            }
        });
    }

    public void populateMembers(){
        final List<User> members = app.getProjectMembers();
        ArrayAdapter<User> projectAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, members) {

            private View get(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                }

                TextView textView = convertView.findViewById(android.R.id.text1);
                User user = getItem(position);
                textView.setText(user.getName());
                return convertView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return get(position, convertView, parent);
            }

        };
        projectMembersListView.setAdapter(projectAdapter);
    }

    private boolean validateInput() {
        if (projectNameEditText.getText().toString().isEmpty()) {
            projectNameEditText.setError("Project name should not be empty");
            projectNameEditText.requestFocus();
            return false;
        }

        return true;
    }
}
