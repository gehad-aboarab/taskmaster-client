package com.example.gehad.taskmaster.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.Response;
import com.example.gehad.taskmaster.TaskApplication;
import com.example.gehad.taskmaster.Util;
import com.example.gehad.taskmaster.activities.ProjectInfoActivity;
import com.example.gehad.taskmaster.activities.SectionInfoActivity;
import com.example.gehad.taskmaster.activities.TaskInfoActivity;
import com.example.gehad.taskmaster.entities.Project;
import com.example.gehad.taskmaster.entities.Section;
import com.example.gehad.taskmaster.entities.Task;
import com.example.gehad.taskmaster.entities.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectSectionsActivity extends Activity {

    private int projectId;
    private static TaskApplication app;
    private ViewPager mViewPager;
    private static SectionsPagerAdapter mSectionsPagerAdapter;

    private TextView assignee, date, description;

    public static final int ADD_TASK = 1;
    public static final int EDIT_TASK = 2;
    public static final int DELETE_TASK = 3;

    public static final int EDIT_PROJECT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_sections);

        app = (TaskApplication) getApplication();

        projectId = getIntent().getIntExtra("projectId", -1);
        //numSections = app.getNumSections(projectId);
        getActionBar().setTitle(app.findProject(projectId).getName());

        assignee = (TextView) findViewById(R.id.project_sectionTaskAssignee);
        description = (TextView) findViewById(R.id.project_sectionTaskDescription);
        date = (TextView) findViewById(R.id.project_sectionTaskDate);

        new LoadProjectMembersTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_TASK) {
            if (resultCode == TaskInfoActivity.ADDED) {
                String taskJson = data.getStringExtra("task");
                Task task = new Gson().fromJson(taskJson, Task.class);
                for (Section section : app.getSections(projectId)) {
                    if (section.getId() == task.getSectionId()) {
                        section.getTasks().add(task);
                        break;
                    }
                }
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == EDIT_PROJECT) {
            if (resultCode == ProjectInfoActivity.PROJECT_EDITED) {
                String projectJson = data.getStringExtra("projectJson");
                Project project = new Gson().fromJson(projectJson, Project.class);
                for (int i = 0; i < app.getProjects().size(); ++i) {
                    if (app.getProjects().get(i).getProjectId() == project.getProjectId()) {
                        app.getProjects().set(i, project);
                        getActionBar().setTitle(project.getName());
                        break;
                    }
                }
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_sections, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_projectInfo) {
            Intent menu = new Intent(getApplicationContext(), ProjectInfoActivity.class);
            menu.putExtra("projectId", projectId);
            startActivityForResult(menu, EDIT_PROJECT);
            return true;
        } else if (id == R.id.menu_deleteProject) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog.OnClickListener listener = new DialogInterface.OnClickListener() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        final String servicePath = "/projects/" + projectId + "?token=" + app.getToken();

                        new AsyncTask<Void, Void, Boolean>() {
                            private Response response;

                            @Override
                            protected Boolean doInBackground(Void... voids) {
                                try {
                                    response = Util.delete(app.getBaseUrl() + servicePath, new HashMap<String, Object>());
                                    return true;
                                } catch (IOException e) {
                                    return false;
                                }
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                if (response.status != HttpURLConnection.HTTP_OK && response.status != 204)
                                    return;
                                for (int i = 0; i < app.getProjects().size(); ++i) {
                                    if (app.getProjects().get(i).getProjectId() == projectId) {
                                        app.getProjects().remove(i);
                                        break;
                                    }
                                }

                                finish();
                            }
                        }.execute();
                    }
                }
            };

            AlertDialog dialog = builder.setTitle("Delete project?")
                    .setMessage("Are you sure you want to delete this project and its tasks?\n This action cannot be undone!")
                    .setPositiveButton("Confirm", listener)
                    .setNegativeButton("Cancel", listener)
                    .show();

        } else if (id == R.id.menu_addTask) {
            int itemIndex = mViewPager.getCurrentItem();
            if(itemIndex == app.getCurrentProject().getSections().size()){
                PlaceholderFragment fragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(itemIndex-1);
                Section section = app.getSections(projectId).get(fragment.getSectionNumber());
                Intent intent = new Intent(getApplicationContext(), TaskInfoActivity.class);
                intent.putExtra("newTask", true);
                intent.putExtra("projectId", projectId);
                intent.putExtra("sectionId", section.getId());
                startActivityForResult(intent, ADD_TASK);
                return true;
            }
            else {
                PlaceholderFragment fragment = (PlaceholderFragment) mSectionsPagerAdapter.getItem(itemIndex);
                Section section = app.getSections(projectId).get(fragment.getSectionNumber());
                Intent intent = new Intent(getApplicationContext(), TaskInfoActivity.class);
                intent.putExtra("newTask", true);
                intent.putExtra("projectId", projectId);
                intent.putExtra("sectionId", section.getId());
                startActivityForResult(intent, ADD_TASK);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadProjectMembersTask extends AsyncTask<Void, Void, Boolean> {
        private Response response;
        private Gson gson = new Gson();

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String servicePath = "/projects/" + projectId + "/members?token=" + app.getToken();
                response = Util.get(app.getBaseUrl() + servicePath);
                if (response.status != HttpURLConnection.HTTP_OK)
                    return false;

                Type listType = new TypeToken<ArrayList<User>>() {
                }.getType();
                ArrayList<User> members = gson.fromJson(response.body, listType);
                app.setProjectMembers(members);

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_PROJECT_ID = "project_id";
        private int projectId;
        private List<Section> sections;
        private int sectionNumber;

        public PlaceholderFragment() {
        }

        public int getSectionNumber() {
            return sectionNumber;
        }

        public static PlaceholderFragment newInstance(int sectionNumber, int projectId) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.sectionNumber = sectionNumber;
            fragment.projectId = projectId;
            fragment.sections = app.getSections(projectId);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putInt(ARG_PROJECT_ID, projectId);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_project_sections, container, false);
            TextView sectionHeaderTextView = (TextView) rootView.findViewById(R.id.project_sectionTitleTextView);
            TextView sectionTaskCountTextView = (TextView) rootView.findViewById(R.id.project_sectionTaskCountTextView);
            LinearLayout sectionHeaderLayout = (LinearLayout) rootView.findViewById(R.id.project_sectionsHeaderLayout);

            final int sectionIdx = getArguments().getInt(ARG_SECTION_NUMBER);
            final int projectId = getArguments().getInt(ARG_PROJECT_ID);

            if (sectionIdx == sections.size()) {
                sectionHeaderTextView.setText("+ Add new section");
                sectionHeaderLayout.setBackgroundColor(Color.parseColor("#000000"));
                sectionTaskCountTextView.setText("");
            } else {
                final Section section = sections.get(sectionIdx);
                sectionHeaderTextView.setText(section.getName());
                sectionHeaderLayout.setBackgroundColor(Color.parseColor(section.getColour()));
                ListView listView = rootView.findViewById(R.id.project_sectionListView);
                ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();

                if (section.getTasks().size() > 0)
                    sectionTaskCountTextView.setText(Integer.toString(section.getTasks().size()));


                for (Task t : section.getTasks()) {
                    String assigneeName = null;

                    if (t.getUserId() != null) {
                        Log.d("GEHAD_TAG", app.getProjectMembers().toString());
                        for (User u : app.getProjectMembers()) {
                            if (t.getUserId() == u.getUserId()) {
                                assigneeName = u.getName();
                            }
                        }
                        if(assigneeName == null){
                            assigneeName = "?";
                        }
                    }

                    Map<String, String> map = new HashMap<String, String>();
                    map.put("taskId", "" + t.getTaskId());
                    map.put("name", t.getName());
                    map.put("date", t.getDate() == null ? null : "  "+t.getDate().toString());
                    map.put("description", t.getDescription() == null ? null : t.getDescription());
                    map.put("assignee", t.getUserId() == null ? null : Character.toString(assigneeName.charAt(0)));
                    data.add(map);
                }

                int resource = R.layout.list_item_task_section;

                String[] from = {"name", "date", "description", "assignee"};
                int[] to = {R.id.project_sectionTaskTitle,
                        R.id.project_sectionTaskDate,
                        R.id.project_sectionTaskDescription,
                        R.id.project_sectionTaskAssignee};

                SimpleAdapter simpleAdapter = new SimpleAdapter(rootView.getContext(), data, resource, from, to) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_task_section, parent, false);
                        }

                        Map<String, String> entryData = (Map<String, String>) getItem(position);

                        TextView nameTextView = convertView.findViewById(R.id.project_sectionTaskTitle);
                        TextView descriptionTextView = convertView.findViewById(R.id.project_sectionTaskDescription);
                        TextView assigneeTextView = convertView.findViewById(R.id.project_sectionTaskAssignee);
                        TextView dateTextView = convertView.findViewById(R.id.project_sectionTaskDate);

                        nameTextView.setText(entryData.get("name"));

                        if (entryData.get("description") != null && !entryData.get("description").isEmpty())
                            descriptionTextView.setText(entryData.get("description"));
                        else
                            descriptionTextView.setVisibility(View.GONE);

                        if (entryData.get("date") != null && !entryData.get("date").isEmpty())
                            dateTextView.setText(entryData.get("date"));
                        else
                            dateTextView.setVisibility(View.GONE);

                        if (entryData.get("assignee") != null && !entryData.get("assignee").isEmpty())
                            assigneeTextView.setText(entryData.get("assignee"));
                        else
                            assigneeTextView.setVisibility(View.GONE);

                        convertView.setTag(entryData.get("taskId"));

                        return convertView;
                    }

                };

                listView.setAdapter(simpleAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(), TaskInfoActivity.class);
                        intent.putExtra("taskId", view.getTag().toString());
                        intent.putExtra("projectId", projectId);
                        intent.putExtra("sectionId", section.getId());
                        startActivityForResult(intent, 2);
                    }
                });
            }

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SectionInfoActivity.class);
                    if (getArguments().getInt(ARG_SECTION_NUMBER) == sections.size()) {
                        intent.putExtra("addFlag", true);
                    }
                    if (sectionIdx != sections.size())
                        intent.putExtra("section", new Gson().toJson(sections.get(sectionIdx)));
                    startActivityForResult(intent, 9);
                }
            };

            sectionHeaderLayout.setOnClickListener(listener);
            sectionHeaderTextView.setOnClickListener(listener);
            sectionTaskCountTextView.setOnClickListener(listener);

            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 2) {
                if (resultCode == TaskInfoActivity.EDITED) {
                    String taskJson = data.getStringExtra("task");
                    Task task = new Gson().fromJson(taskJson, Task.class);
                    List<Task> toSort = null;

                    for (Section section : sections) {
                        int taskIndex = section.getTaskIndex(task.getTaskId());
                        if (taskIndex != -1) {
                            if (task.getSectionId() == section.getId()) {
                                section.getTasks().set(taskIndex, task);
                            } else {
                                section.removeTask(task.getTaskId());
                            }
                        } else if (task.getSectionId() == section.getId()) {
                            section.getTasks().add(task);
                            toSort = section.getTasks();
                        }
                    }

                    if (toSort != null) {
                        Collections.sort(toSort, new Comparator<Task>() {
                            @Override
                            public int compare(Task o1, Task o2) {
                                return o1.getTaskId() - o2.getTaskId();
                            }
                        });
                    }

                    mSectionsPagerAdapter.notifyDataSetChanged();
                } else if (resultCode == TaskInfoActivity.DELETED) {
                    int taskId = data.getIntExtra("taskId", -1);
                    for (Section section : sections) {
                        for (int i = 0; i < section.getTasks().size(); ++i) {
                            if (section.getTasks().get(i).getTaskId() == taskId) {
                                section.getTasks().remove(i);
                                mSectionsPagerAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }

            } else {
                if (resultCode == RESULT_OK) {
                    Section section = new Gson().fromJson(data.getStringExtra("section"), Section.class);
                    sections.add(sections.size(), section);
                    mSectionsPagerAdapter.addFragment(PlaceholderFragment.newInstance(sections.size(), projectId));
                    mSectionsPagerAdapter.notifyDataSetChanged();
                } else if (resultCode == SectionInfoActivity.DELETED) {
                    int sectionId = data.getIntExtra("sectionId", -1);
                    for (Section s : app.getSections(projectId)) {
                        if (sectionId == s.getId()) {
                            app.getSections(projectId).remove(s);
                            mSectionsPagerAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                } else if (resultCode == SectionInfoActivity.EDITED) {
                    Log.d("MY_TAG", "Handling result successfully");
                    Log.d("MY_TAG", app.getSections(projectId).toString());
                    mSectionsPagerAdapter.notifyDataSetChanged();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        }

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            for (int i = 0; i <= app.getNumSections(projectId); ++i) {
                fragments.add(PlaceholderFragment.newInstance(i, projectId));
            }
        }

        public void addFragment(Fragment fragment){
            fragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
//            return PlaceholderFragment.newInstance(position, projectId);
        }

        @Override
        public int getCount() {
            return app.getNumSections(projectId) + 1;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

}
