package com.example.gehad.taskmaster.activities;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.Response;
import com.example.gehad.taskmaster.TaskApplication;
import com.example.gehad.taskmaster.Util;
import com.example.gehad.taskmaster.entities.User;

import java.io.IOException;

public class AddMemberActivity extends Activity {
    private Button inviteButton;
    private EditText emailEditText;
    static TaskApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        app = (TaskApplication) getApplication();

        emailEditText = (EditText) findViewById(R.id.addMember_emailEditText);
        inviteButton = (Button) findViewById(R.id.addMember_inviteButton);


        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inviteeEmail = emailEditText.getText().toString();

                if(inviteeEmail.isEmpty()) {
                    emailEditText.setError("Please enter an email address");
                    emailEditText.requestFocus();
                } else {
                    if(validate(inviteeEmail))
                        new InviteMemberTask().execute();
                    else
                        Toast.makeText(AddMemberActivity.this, "The member you are trying to invite already exists in this project.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean validate(String email){
        for(User u : app.getProjectMembers()){
            if(u.getEmail().equals(email))
                return false;
        }
        return true;
    }

    class InviteMemberTask extends AsyncTask<Void, Void, Boolean>{
        String inviteeEmail = emailEditText.getText().toString();
        Response response;

        @Override
        protected Boolean doInBackground(Void... voids) {
            int projectId = app.getCurrentProject().getProjectId();
            String servicePath = "/invitations?token=" + app.getToken();
            String[] keys = { "projectId","inviteeEmail" };
            Object[] values = { projectId, inviteeEmail };
            try {
                response = Util.post(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                if(response.status != 202)
                    return false;
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                Toast.makeText(AddMemberActivity.this, "Invitation sent.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(AddMemberActivity.this, response.body, Toast.LENGTH_LONG).show();
            }
        }
    }
}
