package com.example.gehad.taskmaster.entities;

public class Invitation {
    private int invitationId;
    private User inviter;
    private User invitee;
    private Project project;
    private int status;

    public static final int PENDING = 0;
    public static final int ACCEPTED = 1;
    public static final int DENIED = 2;

    public Invitation(int invitationId, User inviter, User invitee, Project project, int status) {
        this.invitationId = invitationId;
        this.inviter = inviter;
        this.invitee = invitee;
        this.project = project;
        this.status = status;
    }

    @Override
    public String toString() {
        return "ProjectInvitation{" +
                "invitationId=" + invitationId +
                ", inviter=" + inviter +
                ", invitee=" + invitee +
                ", project=" + project +
                ", status=" + status +
                '}';
    }

    public int getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(int invitationId) {
        this.invitationId = invitationId;
    }

    public User getInviter() {
        return inviter;
    }

    public void setInviter(User inviter) {
        this.inviter = inviter;
    }

    public User getInvitee() {
        return invitee;
    }

    public void setInvitee(User invitee) {
        this.invitee = invitee;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
