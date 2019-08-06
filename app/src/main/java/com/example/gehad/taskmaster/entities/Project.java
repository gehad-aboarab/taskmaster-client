package com.example.gehad.taskmaster.entities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Project {
    private int projectId;
    private String name;
    private String description;
    private List<Section> sections;
    //private int iconId;

    public Project(int projectId, String name, String description) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return projectId == project.projectId &&
                Objects.equals(name, project.name) &&
                Objects.equals(description, project.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, name, description);
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectId:" + projectId +
                ", name:\"" + name + '\"' +
                ", description:\"" + description + '\"' +
                '}';
    }

    public List<Task> fetchTasks() throws SQLException {
        return null;
    }

}
