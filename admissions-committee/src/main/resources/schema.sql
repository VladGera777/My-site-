--DROP DATABASE IF EXISTS admissions_mgmt;
--CREATE DATABASE admissions_mgmt;
--USE admissions_mgmt;
DROP TABLE IF EXISTS Users, Applicants, Faculties, Scores, Enrollment_Requests, Subjects, Faculties_Subjects, Statements;
CREATE TABLE Users (
ID bigint PRIMARY KEY AUTO_INCREMENT,
username varchar(50) NOT NULL UNIQUE,
email varchar(255) NOT NULL UNIQUE,
password varchar(255) NOT NULL,
role varchar(255) DEFAULT 'USER',
is_blocked boolean DEFAULT 'false',
is_enabled boolean DEFAULT 'true'
);

CREATE TABLE Applicants (
ID bigint PRIMARY KEY AUTO_INCREMENT,
first_name varchar(255) NOT NULL,
last_name varchar(255) NOT NULL,
city varchar(255) NOT NULL,
region varchar(255) NOT NULL,
educational_institution varchar(255) NOT NULL,
certificate varchar(255),
user_ID bigint NOT NULL UNIQUE,
FOREIGN KEY (user_ID) REFERENCES Users(ID)
);

CREATE TABLE Faculties (
ID bigint PRIMARY KEY AUTO_INCREMENT,
name varchar(255) NOT NULL UNIQUE,
budget_places int NOT NULL,
total_places int NOT NULL
);

CREATE TABLE Subjects (
ID bigint PRIMARY KEY AUTO_INCREMENT,
name varchar(255) NOT NULL
);

CREATE TABLE Faculties_Subjects (
faculty_ID bigint NOT NULL,
subject_ID bigint NOT NULL,
FOREIGN KEY (faculty_ID) REFERENCES Faculties(ID),
FOREIGN KEY (subject_ID) REFERENCES Subjects(ID)
);

CREATE TABLE Scores (
ID bigint PRIMARY KEY AUTO_INCREMENT,
applicant_ID bigint NOT NULL,
subject_name varchar(255) NOT NULL,
result int NOT NULL,
FOREIGN KEY (applicant_ID) REFERENCES Applicants(ID)
);


CREATE TABLE Enrollment_Requests (
ID bigint PRIMARY KEY AUTO_INCREMENT,
applicant_ID bigint NOT NULL,
faculty_ID bigint NOT NULL,
requested_on timestamp DEFAULT CURRENT_TIMESTAMP,
points int DEFAULT -1,
status varchar(10) DEFAULT 'PENDING',
temp_status varchar(10) DEFAULT 'PENDING',
FOREIGN KEY (applicant_ID) REFERENCES Applicants(ID),
FOREIGN KEY (faculty_ID) REFERENCES Faculties(ID)
);
