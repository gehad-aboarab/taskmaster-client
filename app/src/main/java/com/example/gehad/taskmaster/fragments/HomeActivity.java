package com.example.gehad.taskmaster.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.Response;
import com.example.gehad.taskmaster.TaskApplication;
import com.example.gehad.taskmaster.Util;
import com.example.gehad.taskmaster.activities.AddProjectActivity;
import com.example.gehad.taskmaster.activities.TaskInfoActivity;
import com.example.gehad.taskmaster.entities.Invitation;
import com.example.gehad.taskmaster.entities.Project;
import com.example.gehad.taskmaster.entities.Task;
import com.example.gehad.taskmaster.entities.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class HomeActivity extends Activity {
    private static TaskApplication app;
    private static SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private static final int PROJECTS_FRAGMENT = 1;
    private static final int TASKS_FRAGMENT = 2;
    private static final int INVITATIONS_FRAGMENT = 3;

    private static final int ADD_PROJECT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        app = (TaskApplication) getApplication();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_addProject) {
            Intent intent = new Intent(this, AddProjectActivity.class);
            intent.putExtra("newProject", true);
            startActivityForResult(intent, ADD_PROJECT);
            return true;
        } else if (id == R.id.menu_logout) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_PROJECT) {
            if (resultCode == AddProjectActivity.ADDED) {
                String projectJson = data.getStringExtra("project");
                Project project = new Gson().fromJson(projectJson, Project.class);
                app.getProjects().add(project);
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static class ProjectsPopulateTask extends AsyncTask<Void, Void, Boolean> {

        private View rootView;
        private Response response;
        private Gson gson = new Gson();
        private Exception exception;

        ProjectsPopulateTask(View rootView) {
            this.rootView = rootView;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                int userId = app.getLoggedUser().getUserId();
                String token = app.getToken();
                String servicePath = "/projects/by-user/" + userId + "?token=" + token;

                response = Util.get(app.getBaseUrl() + servicePath);
                if (response.status != HttpURLConnection.HTTP_OK)
                    return false;

                return true;
            } catch (IOException e) {
                exception = e;
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                ListView projectsListView = (ListView) rootView.findViewById(R.id.home_projectsFragListView);
                Log.d("GEHAD_LOG", response.body);
                Type listType = new TypeToken<ArrayList<Project>>() {
                }.getType();
                List<Project> projects = gson.fromJson(response.body, listType);
                app.setProjects(projects);

                ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

                for (Project project : app.getProjects()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("project_name", project.getName());
                    data.add(map);
                }

                int resource = R.layout.list_item_fragment_projects;
                String[] from = {"project_name"};
                int[] to = {R.id.home_listItemProjectName};

                SimpleAdapter adapter = new SimpleAdapter(rootView.getContext(), data, resource, from, to);
                projectsListView.setAdapter(adapter);
                projectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        int selectedProjectId = app.getProjects().get(i).getProjectId();
                        app.setCurrentProject(app.getProjects().get(i));
                        Intent intent = new Intent(rootView.getContext(), ProjectSectionsActivity.class);
                        intent.putExtra("projectId", selectedProjectId);
                        rootView.getContext().startActivity(intent);
                    }
                });

            } else {
                Toast.makeText(rootView.getContext(),
                        "An error occurred while downloading the projects. Try again later...",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class TasksPopulateTask extends AsyncTask<Void, Void, Boolean> {
        private int errorCode = 0;
        private View rootView;
        private String tasksJson;
        private String assigneeName;
        private final String baseUrl = app.getBaseUrl();
        private Gson gson = new Gson();

        TasksPopulateTask(View rootView) {
            this.rootView = rootView;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                int userId = app.getLoggedUser().getUserId();
                String token = app.getToken();
                String servicePath = "/tasks/by-user/" + userId + "?token=" + token;
                assigneeName = app.loggedUser.getName();

                URL url = new URL(baseUrl + servicePath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    errorCode = 1;
                    return false;
                } else {
                    InputStream inputStream = connection.getInputStream();
                    Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                    tasksJson = scanner.next();
                    return true;
                }

            } catch (IOException e) {
                errorCode = 2;
                e.printStackTrace();
                return false;
            }
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                app.setTasks(new ArrayList<Task>());

                Type listType = new TypeToken<ArrayList<Task>>() {
                }.getType();
                List<Task> tasks = gson.fromJson(tasksJson, listType);
                app.setTasks(tasks);
                ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

                ListView tasksListView = (ListView) rootView.findViewById(R.id.home_tasksFragListView);
                for (Task task : app.getTasks()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("task_name", task.getName());
                    map.put("task_date", task.getDate() != null ? "  " + task.getDate().toString() : "  No Due Date");
                    String projectName = app.findProject(task.getProjectId()).getName();
                    map.put("task_project", projectName);
                    map.put("task_assignee", Character.toString(assigneeName.charAt(0)));
                    data.add(map);
                }

                int resource = R.layout.list_item_fragment_tasks;
                String[] from = {"task_name", "task_date", "task_project", "task_assignee"};
                int[] to = {R.id.home_listItemTaskName, R.id.home_listItemTaskDate, R.id.home_listItemTaskProjectName, R.id.home_listItemTaskAssignee};

                SimpleAdapter adapter = new SimpleAdapter(rootView.getContext(), data, resource, from, to);
                tasksListView.setAdapter(adapter);
                tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final int selectedTaskId = app.getTasks().get(i).getTaskId();
                        final int selectedTaskProjectId = app.getTasks().get(i).getProjectId();
                        app.setCurrentProject(app.findProject(selectedTaskProjectId));
                        final int selectedTaskSectionId = app.findTaskSection(selectedTaskId).getId();

                        new AsyncTask<Void, Void, Boolean>() {
                            private Response response;
                            private Gson gson = new Gson();

                            @Override
                            protected Boolean doInBackground(Void... voids) {
                                try {
                                    String servicePath = "/projects/" + selectedTaskProjectId + "/members?token=" + app.getToken();
                                    response = Util.get(app.getBaseUrl() + servicePath);
                                    if (response.status != HttpURLConnection.HTTP_OK)
                                        return false;

                                    return true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                if (aBoolean) {
                                    Type listType = new TypeToken<ArrayList<User>>() {
                                    }.getType();
                                    ArrayList<User> members = gson.fromJson(response.body, listType);
                                    app.setProjectMembers(members);

                                    Intent intent = new Intent(rootView.getContext(), TaskInfoActivity.class);
                                    intent.putExtra("taskId", Integer.toString(selectedTaskId));
                                    intent.putExtra("sectionId", selectedTaskSectionId);
                                    intent.putExtra("projectId", selectedTaskProjectId);
                                    rootView.getContext().startActivity(intent);
                                } else {
                                    Log.d("MY_TAG", "???");
                                }
                            }
                        }.execute();

                    }
                });
            } else {
                if (errorCode != 0) {
                    Toast.makeText(rootView.getContext(), "An error occured! Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public static class InvitationsPopulateTask extends AsyncTask<Void, Void, Boolean> {
        private View rootView;
        private Gson gson = new Gson();
        Response response;

        InvitationsPopulateTask(View rootView) {
            this.rootView = rootView;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String baseUrl = app.getBaseUrl();
            int userId = app.getLoggedUser().getUserId();
            String token = app.getToken();
            String servicePath = "/invitations/by-user/" + userId + "?token=" + token;

            try {
                response = Util.get(baseUrl + servicePath);
                return true;
            } catch (IOException e) {
                return false;
            }
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                app.setInvitations(new ArrayList<Invitation>());
                Type listType = new TypeToken<ArrayList<Invitation>>() {
                }.getType();

                List<Invitation> invitations = gson.fromJson(response.body, listType);
                List<Invitation> tempInvitations = new ArrayList<>();

                for (Invitation i : invitations) {
                    if (i.getStatus() == 0)
                        tempInvitations.add(i);
                }

                app.setInvitations(tempInvitations);
                ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

                ListView invitationsListView = (ListView) rootView.findViewById(R.id.home_invitationsFragListView);
                for (Invitation invitation : app.getInvitations()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("project_name", invitation.getProject().getName());
                    map.put("inviter", invitation.getInviter().getName() + " has invited you to join their project");
                    data.add(map);
                }

                int resource = R.layout.list_item_fragment_invitations;
                String[] from = {"project_name", "inviter"};
                int[] to = {R.id.home_listItemInvitationProjectName, R.id.home_listItemInviter};

                SimpleAdapter adapter = new SimpleAdapter(rootView.getContext(), data, resource, from, to);
                invitationsListView.setAdapter(adapter);
                invitationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final int selectedInvitationId = app.getInvitations().get(i).getInvitationId();
                        final Invitation selectedInvitation = app.getInvitations().get(i);

                        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                        AlertDialog.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @SuppressLint("StaticFieldLeak")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == AlertDialog.BUTTON_POSITIVE) {
                                    new AsyncTask<Void, Void, Boolean>() {
                                        private Response response;
                                        private Gson gson = new Gson();

                                        @Override
                                        protected Boolean doInBackground(Void... voids) {
                                            try {
                                                String servicePath = "/invitations/" + selectedInvitationId + "/accept";
                                                String[] keys = {"token"};
                                                Object[] values = {app.getToken()};
                                                response = Util.post(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                                                if (response.status != 202) {
                                                    Log.d("GEHAD_TAG", response.status + " " + app.getToken());
                                                    return false;
                                                }
                                                Log.d("GEHAD_TAG", response.body);
                                                return true;
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                return false;
                                            }
                                        }

                                        @Override
                                        protected void onPostExecute(Boolean aBoolean) {
                                            if (aBoolean) {
                                                for (int i = 0; i < app.getInvitations().size(); ++i) {
                                                    if (app.getInvitations().get(i).getInvitationId() == selectedInvitationId) {
                                                        app.getInvitations().remove(i);
                                                        mSectionsPagerAdapter.notifyDataSetChanged();
                                                        break;
                                                    }
                                                }

                                                Project project = selectedInvitation.getProject();
                                                app.getProjects().add(project);
                                                mSectionsPagerAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }.execute();
                                } else if (which == AlertDialog.BUTTON_NEGATIVE) {
                                    new AsyncTask<Void, Void, Boolean>() {
                                        private Response response;
                                        private Gson gson = new Gson();

                                        @Override
                                        protected Boolean doInBackground(Void... voids) {
                                            try {
                                                String servicePath = "/invitations/" + selectedInvitationId + "/decline";
                                                String[] keys = {"token"};
                                                Object[] values = {app.getToken()};
                                                response = Util.post(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                                                if (response.status != 202) {
                                                    Log.d("GEHAD_TAG", response.status + " " + app.getToken());
                                                    return false;
                                                }
                                                Log.d("GEHAD_TAG", response.body);
                                                return true;
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                return false;
                                            }
                                        }

                                        @Override
                                        protected void onPostExecute(Boolean aBoolean) {
                                            if (aBoolean) {
                                                for (int i = 0; i < app.getInvitations().size(); ++i) {
                                                    if (app.getInvitations().get(i).getInvitationId() == selectedInvitationId) {
                                                        app.getInvitations().remove(i);
                                                        mSectionsPagerAdapter.notifyDataSetChanged();
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }.execute();
                                }
                            }
                        };
                        AlertDialog dialog = builder.setTitle("Accept Invitation")
                                .setMessage(selectedInvitation.getInviter().getName()
                                        + " has invited you to join their project "
                                        + selectedInvitation.getProject().getName()
                                        + "\nWould you like to accept their invitation?")
                                .setPositiveButton("Accept", listener)
                                .setNegativeButton("Decline", listener)
                                .show();
                    }
                });
            } else {
                Toast.makeText(rootView.getContext(), "An error occured! Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView;

            // Projects fragment
            if (getArguments().getInt(ARG_SECTION_NUMBER) == PROJECTS_FRAGMENT) {
                rootView = inflater.inflate(R.layout.fragment_projects, container, false);
                TextView title = (TextView) rootView.findViewById(R.id.home_projectsFragTitle);
                title.setText("  My Projects");

                new ProjectsPopulateTask(rootView).execute();
            }

            // Tasks fragment
            else if (getArguments().getInt(ARG_SECTION_NUMBER) == TASKS_FRAGMENT) {
                rootView = inflater.inflate(R.layout.fragment_tasks, container, false);
                TextView title = (TextView) rootView.findViewById(R.id.home_tasksFragTitle);
                title.setText("  My Tasks");

                new TasksPopulateTask(rootView).execute();
            }

            // Invitations fragment
            else {
                rootView = inflater.inflate(R.layout.fragment_invitations, container, false);
                TextView title = (TextView) rootView.findViewById(R.id.home_invitationsFragTitle);
                title.setText("  My Invitations");

                new InvitationsPopulateTask(rootView).execute();
            }
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Projects";
                case 1:
                    return "Tasks";
                case 2:
                    return "Invitations";
            }
            return null;
        }
    }
}
