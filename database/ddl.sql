-- =====================================================================
-- DDL COMMANDS (Data Definition Language)
-- College Course Registration System
-- =====================================================================

CREATE DATABASE IF NOT EXISTS collegecourseregistrationsystem;
USE collegecourseregistrationsystem;

-- 1. Department
CREATE TABLE IF NOT EXISTS Department (
    department_id INT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL
);

-- 2. Student (includes year and semester)
CREATE TABLE IF NOT EXISTS Student (
    student_id INT PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    department_id INT,
    year INT NOT NULL,
    semester INT NOT NULL,
    FOREIGN KEY (department_id)
        REFERENCES Department(department_id)
);

-- 3. Faculty
CREATE TABLE IF NOT EXISTS Faculty (
    faculty_id INT PRIMARY KEY,
    faculty_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    department_id INT,
    FOREIGN KEY (department_id)
        REFERENCES Department(department_id)
);

-- 4. Course (includes year and semester)
CREATE TABLE IF NOT EXISTS Course (
    course_id INT PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    department_id INT,
    year INT NOT NULL,
    semester INT NOT NULL,
    FOREIGN KEY (department_id)
        REFERENCES Department(department_id)
);

-- 5. Semester
CREATE TABLE IF NOT EXISTS Semester (
    semester_id INT PRIMARY KEY,
    semester_name VARCHAR(50) NOT NULL,
    academic_year VARCHAR(20) NOT NULL
);

-- 6. Course Offering
CREATE TABLE IF NOT EXISTS Course_Offering (
    offering_id INT PRIMARY KEY,
    course_id INT NOT NULL,
    semester_id INT NOT NULL,
    FOREIGN KEY (course_id)
        REFERENCES Course(course_id),
    FOREIGN KEY (semester_id)
        REFERENCES Semester(semester_id)
);

-- 7. Classroom
CREATE TABLE IF NOT EXISTS Classroom (
    classroom_id INT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL,
    capacity INT NOT NULL
);

-- 8. Course Section
CREATE TABLE IF NOT EXISTS Course_Section (
    section_id INT PRIMARY KEY,
    offering_id INT NOT NULL,
    faculty_id INT NOT NULL,
    classroom_id INT NOT NULL,
    FOREIGN KEY (offering_id)
        REFERENCES Course_Offering(offering_id),
    FOREIGN KEY (faculty_id)
        REFERENCES Faculty(faculty_id),
    FOREIGN KEY (classroom_id)
        REFERENCES Classroom(classroom_id)
);

-- 9. Enrollment
CREATE TABLE IF NOT EXISTS Enrollment (
    enrollment_id INT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    enrollment_date DATE,
    status VARCHAR(20),
    grade VARCHAR(5),
    FOREIGN KEY (student_id)
        REFERENCES Student(student_id),
    FOREIGN KEY (section_id)
        REFERENCES Course_Section(section_id)
);
