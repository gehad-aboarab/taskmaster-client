package com.example.gehad.taskmaster.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.Response;
import com.example.gehad.taskmaster.TaskApplication;
import com.example.gehad.taskmaster.Util;
import com.example.gehad.taskmaster.entities.Section;
import com.example.gehad.taskmaster.entities.Task;
import com.example.gehad.taskmaster.entities.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class TaskInfoActivity extends Activity {
    private EditText nameEditText, descriptionEditText, dateEditText;
    private Spinner assignedUserSpinner;
    private Spinner taskSectionSpinner;
    private Button saveButton, deleteButton;
    //    private MenuItem completeMenuItem;
    private Menu menu;
    private static TaskApplication app;

    private ArrayAdapter<Section> sectionsAdapter;
    private ArrayAdapter<User> usersAdapter;

    private int projectId;
    private int taskId;
    private int sectionId;

    private boolean newTask;

    public static final int ADDED = 1;
    public static final int EDITED = 2;
    public static final int DELETED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        nameEditText = (EditText) findViewById(R.id.taskInfo_taskNameEditText);
        descriptionEditText = (EditText) findViewById(R.id.taskDescriptionEditText);
        dateEditText = (EditText) findViewById(R.id.taskDateEditText);
        taskSectionSpinner = (Spinner) findViewById(R.id.taskSectionSpinner);
        saveButton = (Button) findViewById(R.id.saveTaskButton);
        deleteButton = (Button) findViewById(R.id.deleteTaskButton);
        assignedUserSpinner = (Spinner) findViewById(R.id.taskUserSpinner);

        app = (TaskApplication) getApplication();

        Intent intent = getIntent();

        newTask = intent.getBooleanExtra("newTask", false);
        projectId = intent.getIntExtra("projectId", -1);
        sectionId = intent.getIntExtra("sectionId", -1);
        load();

        if (newTask) {
            deleteButton.setVisibility(View.INVISIBLE);
//            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) saveButton.getLayoutParams();
//            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        } else {
            taskId = Integer.parseInt(intent.getStringExtra("taskId"));
            loadExistingTask(taskId);
        }
    }

    private void load() {
        final List<User> users = app.getProjectMembers();
        usersAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_spinner_item, users) {
            public View get(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                }

                TextView textView = convertView.findViewById(android.R.id.text1);

                if (position == super.getCount())
                    textView.setText("Unassigned");
                else
                    textView.setText(getItem(position).getName());

                return convertView;
            }

            @Override
            public int getCount() {
                return super.getCount() + 1;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return get(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return get(position, convertView, parent);
            }
        };
        assignedUserSpinner.setAdapter(usersAdapter);

        final List<Section> sections = app.getSections(projectId);
        sectionsAdapter = new ArrayAdapter<Section>(this, android.R.layout.simple_spinner_item, sections) {

            private View get(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                }

                TextView textView = convertView.findViewById(android.R.id.text1);
                Section section = getItem(position);
                textView.setText(section.getName());
                return convertView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return get(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return get(position, convertView, parent);
            }

        };
        taskSectionSpinner.setAdapter(sectionsAdapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Boolean>() {

                    Response response;

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        String dateString = dateEditText.getText().toString().trim();
                        if (!dateString.isEmpty()) {
                            try {
                                Date.valueOf(dateString);
                            } catch (IllegalArgumentException e) {
                                dateEditText.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        dateEditText.setError("Date format should be yyyy-mm-dd");
                                        dateEditText.requestFocus();
                                    }
                                });
                                return false;
                            }
                        }

                        dateEditText.post(new Runnable() {
                            @Override
                            public void run() {
                                dateEditText.setError(null);
                            }
                        });

                        String description = descriptionEditText.getText().toString();
                        String name = nameEditText.getText().toString();
                        Section selectedSection = (Section) taskSectionSpinner.getSelectedItem();
                        int newSectionId = selectedSection.getId();

                        Integer userId = null;
                        try {
                            User user = (User) assignedUserSpinner.getSelectedItem();
                            userId = user.getUserId();
                        } catch (IndexOutOfBoundsException e) {
                        }

                        String[] keys = {"name", "description", "date", "userId", "sectionId"};
                        Object[] values = {name, description, dateString, userId == null ? "" : userId, newSectionId};

                        try {
                            if (newTask) {
                                String servicePath = "/projects/" + projectId +
                                        "/sections/" + sectionId  +
                                        "/tasks" +
                                        "?token=" + app.getToken();
                                response = Util.post(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                                return true;
                            } else {
                                String servicePath = "/projects/" + projectId +
                                        "/sections/" + sectionId +
                                        "/tasks/" + taskId +
                                        "?token=" + app.getToken();
                                response = Util.put(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                                return true;
                            }
                        } catch (IOException e) {
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        if (!success) {
                            if (response == null)
                                Toast.makeText(TaskInfoActivity.this, "Failed to save, please try again later..", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("task", response.body);
                            if (newTask)
                                setResult(ADDED, intent);
                            else
                                setResult(EDITED, intent);
                            finish();
                        }
                    }

                }.execute();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Boolean>() {

                    Response response;

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        try {

                            String servicePath = "/projects/" + projectId
                                    + "/sections/" + sectionId
                                    + "/tasks/" + taskId + "?token=" + app.getToken();
                            response = Util.delete(app.getBaseUrl() + servicePath, new HashMap<String, Object>());
                            if (response.status == HttpURLConnection.HTTP_ACCEPTED)
                                return true;
                            return false;
                        } catch (IOException e) {
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        if (!success) {
                            Toast.makeText(TaskInfoActivity.this, "Bad", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("taskId", taskId);
                            setResult(DELETED, intent);
                            finish();
                        }
                    }
                }.execute();
            }
        });

    }

    private void loadExistingTask(final int taskId) {
        final Task task = app.findTask(taskId);

        if (task.getUserId() == null) {
            assignedUserSpinner.setSelection(assignedUserSpinner.getCount() - 1);
        } else {
            for (int i = 0; i < usersAdapter.getCount() - 1; ++i) {
                User user = (User) usersAdapter.getItem(i);
                if (user.getUserId() == task.getUserId()) {
                    assignedUserSpinner.setSelection(i);
                    break;
                }
            }
        }

        for (int i = 0; i < sectionsAdapter.getCount(); ++i) {
            Section section = sectionsAdapter.getItem(i);
            if (section.getId() == task.getSectionId()) {
                taskSectionSpinner.setSelection(i);
                break;
            }
        }

        nameEditText.setText(task.getName());
        descriptionEditText.setText(task.getDescription() == null ? null : task.getDescription());
        dateEditText.setText(task.getDate() == null ? null : task.getDate().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_task, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_completeTask) {
            new CompleteTaskTask().execute();
            return true;
        } else if (id == R.id.menu_restoreTask) {
            new RestoreTaskTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class CompleteTaskTask extends AsyncTask<Void, Void, Boolean> {
        Response response;

        @Override
        protected Boolean doInBackground(Void... voids) {
            String servicePath = "/projects/" + app.getCurrentProject().getProjectId() + "/tasks/" + taskId + "/completed?token=" + app.getToken();
            try {
                String[] keys = {"completed"};
                Object[] values = {true};

                response = Util.post(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                if (response.status != HttpURLConnection.HTTP_ACCEPTED) {
                    Log.d("GEHAD_TAG", response.status + ": " + response.body);
                    return false;
                }
                return true;
            } catch (IOException e) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                for (Section section : app.getSections(projectId)) {
                    int taskIndex = section.getTaskIndex(taskId);
                    if (taskIndex != -1) {
                        if (taskId == section.getId()) {
                            section.getTasks().get(taskIndex).setCompleted(true);
                            Log.d("GEHAD_LOG", section.getTask(taskId).toString());
                        }
                    }
                }
                finish();
            } else {
                Toast.makeText(TaskInfoActivity.this, "Error occurred.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    class RestoreTaskTask extends AsyncTask<Void, Void, Boolean> {
        Response response;

        @Override
        protected Boolean doInBackground(Void... voids) {
            String servicePath = "/projects/" + app.getCurrentProject().getProjectId() + "/tasks/" + taskId + "/completed?token=" + app.getToken();
            try {
                String[] keys = {"completed"};
                Object[] values = {false};

                response = Util.post(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                if (response.status != HttpURLConnection.HTTP_ACCEPTED) {
                    Log.d("GEHAD_TAG", response.status + ": " + response.body);
                    return false;
                }
                return true;
            } catch (IOException e) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                for (Section section : app.getSections(projectId)) {
                    int taskIndex = section.getTaskIndex(taskId);
                    if (taskIndex != -1) {
                        if (taskId == section.getId()) {
                            section.getTasks().get(taskIndex).setCompleted(false);
                            Log.d("GEHAD_LOG", section.getTask(taskId).toString());
                        }
                    }
                }
                finish();
            } else {
                Toast.makeText(TaskInfoActivity.this, "Error occurred.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
