package com.example.gehad.taskmaster;

import android.app.Application;
import android.content.SharedPreferences;

import com.example.gehad.taskmaster.entities.Invitation;
import com.example.gehad.taskmaster.entities.Project;
import com.example.gehad.taskmaster.entities.Section;
import com.example.gehad.taskmaster.entities.Task;
import com.example.gehad.taskmaster.entities.User;

import java.util.ArrayList;
import java.util.List;

public class TaskApplication extends Application {
    public List<Project> projects;
    public List<Task> tasks;
    public List<User> projectMembers;
    public List<Invitation> invitations;
    public User loggedUser;
    public Project currentProject;
    public String token;
    public Section newSection = null;
//    public SharedPreferences sp;

    public static final String baseUrl = "http://ec2-18-220-107-100.us-east-2.compute.amazonaws.com:8080/myapp";

    public int getNumSections(int projectId) {
        for (Project p : projects) {
            if (p.getProjectId() == projectId) {
                return p.getSections().size();
            }
        }
        return -1;
    }

    public Project findProject(int projectId) {
        for (Project p : projects)
            if (p.getProjectId() == projectId)
                return p;
        return null;
    }

    public Task findTask(int taskId){
        for (Section s : currentProject.getSections()) {
            for (Task t : s.getTasks()) {
                if (t.getTaskId() == taskId)
                    return t;
            }
        }

        return null;
    }

    public Section findTaskSection(int taskId){
        for(Section s : currentProject.getSections()){
            for (Task t : s.getTasks()){
                if (t.getTaskId() == taskId)
                    return s;
            }
        }
        return null;
    }

    public Project getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    public List<Invitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations;
    }

    public List<Section> getSections(int projectId) {
        return findProject(projectId).getSections();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<User> getProjectMembers() {
        return projectMembers;
    }

    public void setProjectMembers(List<User> projectMembers) {
        this.projectMembers = projectMembers;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Section getNewSection() {
        return newSection;
    }

    public void setNewSection(Section newSection) {
        this.newSection = newSection;
    }
}
