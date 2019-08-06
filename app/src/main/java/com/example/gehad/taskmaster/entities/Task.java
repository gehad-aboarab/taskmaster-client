package com.example.gehad.taskmaster.entities;

import java.sql.Date;
import java.util.Objects;

public class Task {
    private int taskId;
    private String description;
    private String name;
    private Date date;
    private int projectId;
    private int sectionId;
    private boolean completed;
    private Integer userId;

    public Task(int taskId, String description, String name, Date date, int projectId, int sectionId, Integer userId, boolean completed) {
        this.taskId = taskId;
        this.description = description;
        this.name = name;
        this.date = date;
        this.projectId = projectId;
        this.sectionId = sectionId;
        this.userId = userId;
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId == task.taskId &&
                projectId == task.projectId &&
                sectionId == task.sectionId &&
                Objects.equals(description, task.description) &&
                Objects.equals(name, task.name) &&
                Objects.equals(date, task.date) &&
                Objects.equals(userId, task.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, description, name, date, projectId, sectionId, userId);
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", projectId=" + projectId +
                ", sectionId=" + sectionId +
                ", userId=" + userId +
                ", completed=" + completed +
                '}';
    }

}
