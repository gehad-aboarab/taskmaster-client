# TaskMaster
Android application that allows users to manage their tasks for different projects. Tasks can be assigned to different members within group projects, and the application allows for easy and user-friendly management of tasks.

## Important
Pre-requisites to successfully running the application is to run the TaskMaster server: https://github.com/gehad-aboarab/taskmaster-server 
This server handles communication with the local database (SQL Dump found in a file wihin the server respository).


## Functionalities
-	Creating new projects
-	Editing project information
-	Inviting members to projects
-	Deleting projects
-	Adding sections to projects
-	Editing section information
-	Deleting sections
-	Adding new tasks to sections
-	Editing task information
-	Completing tasks
-	Assigning tasks to different members in the project
-	Moving tasks to different sections within the project
-	Deleting tasks

## User Flow
1.	User registers their credentials if they do not have an account.
2.	User logins into the system.
3.	The Home page is an activity of 3 fragments:
  a.	The user’s projects, [step 5] 
  b.	their assigned tasks, [step 9]
  c.	invitations they have received from other users to join other projects. [step 10]
4.	The Home page has a menu that consists of two items:
  a.	Add Project: allows the user to create a new project,
  b.	Log out: logs the user out of the system.
5.	When the user selects one of their projects, the project’s sections are displayed, with all the tasks listed under their corresponding sections.
6.	When the user selects a task, the task’s information is displayed and can be edited.
7.	When the user swipes to the last fragment of any project’s sections activity, they can choose to add a new section.
8.	The project’s sections page contains 3 menu items:
  a.	Add Task: allows the user to add new tasks to the current project.
  b.	Project Info: displays the project’s information, also editable.
    i.	Through project info, a user can choose to add members to the current project.
  c.	Delete project: allows the user to delete the current project.
9.	From the Home page, if the user selects one of their assigned tasks from the second fragment, the task’s information will be displayed and can edited and saved, or deleted.
10.	From the Home page, the user can also accept or decline the invitations they receive from other users that are displayed on the third fragment. 

Screenshots, documentation and demo of the application can be found at: https://drive.google.com/open?id=1kzURFlaIptY6pvZ4PyHmBzYXX4Voq17J
