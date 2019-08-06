package com.example.gehad.taskmaster.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.gehad.taskmaster.R;
import com.example.gehad.taskmaster.Response;
import com.example.gehad.taskmaster.TaskApplication;
import com.example.gehad.taskmaster.Util;
import com.example.gehad.taskmaster.entities.Section;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SectionInfoActivity extends Activity {
    private TaskApplication app;
    private EditText sectionNameEditText;
    private EditText sectionDescriptionEditText;
    private Button deleteButton, saveButton;
    private boolean addFlag = false;

    private ImageButton checkedButton = null;

    private Section section;

    public static final int ADDED = 1;
    public static final int EDITED = 2;
    public static final int DELETED = 3;

    public void scaleButton(ImageButton b, boolean up) {
        if (b == null)
            return;

        Animator animator;
        if (up) {
            animator = AnimatorInflater.loadAnimator(this, R.animator.scale_up);
        } else {
            animator = AnimatorInflater.loadAnimator(this, R.animator.scale_down);
        }

        animator.setTarget(b);
        animator.setDuration(100);
        animator.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_info);

        app = (TaskApplication) getApplication();
        sectionNameEditText = (EditText) findViewById(R.id.sectionInfo_sectionName);
        sectionDescriptionEditText = (EditText) findViewById(R.id.sectionInfo_sectionDescription);
        saveButton = (Button) findViewById(R.id.sectionInfo_saveButton);
        deleteButton = (Button) findViewById(R.id.sectionInfo_deleteButton);

        addFlag = getIntent().getBooleanExtra("addFlag", false);
        if (addFlag)
            deleteButton.setVisibility(View.GONE);
        else
            deleteButton.setVisibility(View.VISIBLE);

        section = new Gson().fromJson(getIntent().getStringExtra("section"), Section.class);
        if (section != null)
            Log.d("SECTION_TAG", section.toString());
        saveButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {
                if (addFlag) {
                    new AddSectionTask().execute();
                } else {
                    new AsyncTask<Void, Void, Boolean>() {

                        Response response;

                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            String servicePath = "/projects/" + section.getProject_id()
                                    + "/sections/" + section.getId()
                                    + "?token=" + app.getToken();

                            int color = Integer.parseInt(checkedButton.getTag().toString());

                            String[] keys = {"name", "colour", "description"};
                            Object[] values = {sectionNameEditText.getText().toString(), "#" + Integer.toHexString(color).substring(2), sectionDescriptionEditText.getText().toString()};

                            try {
                                response = Util.put(app.getBaseUrl() + servicePath, Util.makeMap(keys, values));
                                return response.status == HttpURLConnection.HTTP_OK;
                            } catch (IOException e) {
                                return false;
                            }
                        }

                        @Override
                        protected void onPostExecute(Boolean success) {
                            if (success) {
                                Section newSection = new Gson().fromJson(response.body, Section.class);
                                Log.d("MY_TAG", "new section = " + newSection);

                                List<Section> sections = app.getSections(newSection.getProject_id());
                                Log.d("MY_TAG", "sections = " + sections);
                                boolean found = false;
                                for (int i = 0; i < sections.size(); ++i) {
                                    if (sections.get(i).getId() == newSection.getId()) {
                                        Section oldSection = sections.get(i);
                                        oldSection.setName(newSection.getName());
                                        oldSection.setColour(newSection.getColour());
                                        oldSection.setDescription(newSection.getDescription());
                                        found = true;
                                        break;
                                    }
                                }

                                Intent intent = new Intent();
                                if (found) {
                                    setResult(EDITED, intent);
                                    Log.d("MY_TAG", "Edited successfully");
                                    Log.d("MY_TAG", "sections after edit = " + sections);
                                } else {
                                    setResult(RESULT_CANCELED, intent);
                                    Log.d("MY_TAG", "Edited unsuccessfully");
                                }
                                finish();
                            } else {
                                Toast.makeText(SectionInfoActivity.this, "Error editing section, try again later...", Toast.LENGTH_LONG).show();
                            }
                        }
                    }.execute();
                }

            }

        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SectionInfoActivity.this);
                AlertDialog.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == AlertDialog.BUTTON_POSITIVE) {
                            final String servicePath = "/projects/" + app.getCurrentProject().getProjectId()
                                    + "/sections/" + section.getId() + "?token=" + app.getToken();

                            new AsyncTask<Void, Void, Boolean>() {
                                Response response;

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
                                protected void onPostExecute(Boolean success) {
                                    if (!success) {
                                        Toast.makeText(SectionInfoActivity.this, "Error deleting section, try again later...", Toast.LENGTH_LONG).show();
                                    } else {
                                        Intent intent = new Intent();
                                        intent.putExtra("sectionId", section.getId());
                                        setResult(DELETED, intent);
                                        finish();
                                    }
                                }
                            }.execute();
                        }
                    }
                };

                AlertDialog dialog = builder.setTitle("Delete section?")
                        .setMessage("Are you sure you want to delete this section and its tasks?\n This action cannot be undone!")
                        .setPositiveButton("Confirm", listener)
                        .setNegativeButton("Cancel", listener)
                        .show();

            }
        });


        if (section != null) {
            sectionNameEditText.setText(section.getName());
            sectionDescriptionEditText.setText(section.getDescription());
        }

        LinearLayout buttonsLayout = findViewById(R.id.buttonsLayout);

        TypedArray coloursArray = getResources().obtainTypedArray(R.array.sectionColours);

        for (int i = 0; i < coloursArray.length(); ++i) {
            int colour = coloursArray.getColor(i, Color.BLACK);
            ImageButton imageButton = new ImageButton(this);
            imageButton.setImageDrawable(getDrawable(R.drawable.circle));
            imageButton.setColorFilter(colour);
            imageButton.setPadding(0, 0, 0, 0);
            imageButton.setTag(colour);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            imageButton.setLayoutParams(layoutParams);
            if (!addFlag && colour == Color.parseColor(section.getColour()))
                checkedButton = imageButton;
            if (addFlag && i == 0)
                checkedButton = imageButton;
            //imageButton.setBackgroundColor(colour + 100);
            imageButton.setBackgroundColor(Color.TRANSPARENT);

            buttonsLayout.addView(imageButton);

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v == checkedButton)
                        return;

                    scaleButton(checkedButton, false);
                    scaleButton((ImageButton) v, true);
                    checkedButton = (ImageButton) v;
                }
            });
        }

        scaleButton(checkedButton, true);
    }

    class AddSectionTask extends AsyncTask<Void, Void, Void> {

        private Section section;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                section = addSection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("INTENT_TAG", "Set result, returning");
            Intent intent = new Intent();
            intent.putExtra("section", new Gson().toJson(section));
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public Section addSection() throws IOException {
        String baseUrl = app.getBaseUrl();
        String servicePath = "/projects/" + app.getCurrentProject().getProjectId() + "/sections?token=" + app.getToken();

        int color = Integer.parseInt(checkedButton.getTag().toString());

        String[] keys = {"name", "colour", "description"};
        Object[] values = {sectionNameEditText.getText().toString(), "#" + Integer.toHexString(color).substring(2), sectionDescriptionEditText.getText().toString() == null ? null : sectionDescriptionEditText.getText().toString()};

        Response response = Util.post(baseUrl + servicePath, Util.makeMap(keys, values));
        if (response.status != HttpURLConnection.HTTP_OK) {
            Log.d("maaster", "Something went wrong; handle the error here...");
            return null;
        } else {
            Section section = new Gson().fromJson(response.body, Section.class);
            return section;
        }
    }

}
