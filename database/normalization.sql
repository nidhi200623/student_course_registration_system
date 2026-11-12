USE collegecourseregistrationsystem;

CREATE TABLE Student_Contact_Register (
    student_id INT PRIMARY KEY,
    student_name VARCHAR(100),
    contact_numbers VARCHAR(100)
);

INSERT INTO Student_Contact_Register VALUES
(101, 'Santhosh Varma', '9000000001, 9000000002'),
(102, 'Priya Sharma', '9000000003, 9000000004'),
(103, 'Rahul Verma', '9000000005');

SELECT * FROM Student_Contact_Register;

CREATE TABLE Student_Contact (
    student_id INT,
    contact_number VARCHAR(15),
    PRIMARY KEY (student_id, contact_number),
    FOREIGN KEY (student_id)
        REFERENCES Student(student_id)
);

INSERT INTO Student_Contact VALUES
(101, '9000000001'),
(101, '9000000002'),
(102, '9000000003'),
(102, '9000000004'),
(103, '9000000005');

SELECT * FROM Student_Contact;

CREATE TABLE Course_Registration (
    student_id INT,
    course_id INT,
    student_name VARCHAR(100),
    course_name VARCHAR(100),
    grade VARCHAR(5),
    PRIMARY KEY (student_id, course_id)
);

INSERT INTO Course_Registration VALUES
(101, 301, 'Santhosh Varma', 'Python', 'A'),
(101, 302, 'Santhosh Varma', 'DBMS', 'A+'),
(102, 301, 'Priya Sharma', 'Python', 'B+'),
(102, 303, 'Priya Sharma', 'Java', 'A');

CREATE TABLE Registration_Student (
    student_id INT PRIMARY KEY,
    student_name VARCHAR(100)
);

CREATE TABLE Registration_Course (
    course_id INT PRIMARY KEY,
    course_name VARCHAR(100)
);

CREATE TABLE Registration_Details (
    student_id INT,
    course_id INT,
    grade VARCHAR(5),
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id)
        REFERENCES Registration_Student(student_id),
    FOREIGN KEY (course_id)
        REFERENCES Registration_Course(course_id)
);

INSERT INTO Registration_Student VALUES
(101, 'Santhosh Varma'),
(102, 'Priya Sharma');

INSERT INTO Registration_Course VALUES
(301, 'Python'),
(302, 'DBMS'),
(303, 'Java');

INSERT INTO Registration_Details VALUES
(101, 301, 'A'),
(101, 302, 'A+'),
(102, 301, 'B+'),
(102, 303, 'A');

SELECT * FROM Registration_Student;
SELECT * FROM Registration_Course;
SELECT * FROM Registration_Details;

CREATE TABLE Student_Department_Record (
    student_id INT PRIMARY KEY,
    student_name VARCHAR(100),
    department_id INT,
    department_name VARCHAR(100)
);

INSERT INTO Student_Department_Record VALUES
(101, 'Santhosh Varma', 10, 'Artificial Intelligence'),
(102, 'Priya Sharma', 20, 'Computer Science'),
(103, 'Rahul Verma', 10, 'Artificial Intelligence'),
(104, 'Sneha Reddy', 20, 'Computer Science');

CREATE TABLE Student_Information (
    student_id INT PRIMARY KEY,
    student_name VARCHAR(100),
    department_id INT
);

CREATE TABLE Department_Information (
    department_id INT PRIMARY KEY,
    department_name VARCHAR(100)
);

INSERT INTO Student_Information VALUES
(101, 'Santhosh Varma', 10),
(102, 'Priya Sharma', 20),
(103, 'Rahul Verma', 10),
(104, 'Sneha Reddy', 20);

INSERT INTO Department_Information VALUES
(10, 'Artificial Intelligence'),
(20, 'Computer Science');

SELECT
    s.student_id,
    s.student_name,
    d.department_name
FROM Student_Information s
JOIN Department_Information d
ON s.department_id = d.department_id;

CREATE TABLE Course_Instructor_Assignment (
    student_id INT,
    instructor_id INT,
    course_id INT,
    PRIMARY KEY (student_id, instructor_id)
);

INSERT INTO Course_Instructor_Assignment VALUES
(101, 201, 301),
(102, 201, 301),
(103, 202, 302),
(104, 202, 302),
(105, 203, 303);

CREATE TABLE Instructor_Course (
    instructor_id INT PRIMARY KEY,
    course_id INT
);

CREATE TABLE Student_Instructor (
    student_id INT,
    instructor_id INT,
    PRIMARY KEY (student_id, instructor_id),
    FOREIGN KEY (instructor_id)
        REFERENCES Instructor_Course(instructor_id)
);
INSERT INTO Instructor_Course VALUES
(201, 301),
(202, 302),
(203, 303);

INSERT INTO Student_Instructor VALUES
(101, 201),
(102, 201),
(103, 202),
(104, 202),
(105, 203);

SELECT
    si.student_id,
    si.instructor_id,
    ic.course_id
FROM Student_Instructor si
JOIN Instructor_Course ic
ON si.instructor_id = ic.instructor_id;

CREATE TABLE Student_Skill_Language (
    student_id INT,
    skill VARCHAR(50),
    language VARCHAR(50),
    PRIMARY KEY (student_id, skill, language)
);

INSERT INTO Student_Skill_Language VALUES
(101, 'Python', 'English'),
(101, 'Python', 'Hindi'),
(101, 'Java', 'English'),
(101, 'Java', 'Hindi');
CREATE TABLE Student_Skill (
    student_id INT,
    skill VARCHAR(50),
    PRIMARY KEY (student_id, skill)
);

CREATE TABLE Student_Language (
    student_id INT,
    language VARCHAR(50),
    PRIMARY KEY (student_id, language)
);

INSERT INTO Student_Skill VALUES
(101, 'Python'),
(101, 'Java');

INSERT INTO Student_Language VALUES
(101, 'English'),
(101, 'Hindi');

SELECT * FROM Student_Skill;

SELECT * FROM Student_Language;

CREATE TABLE Student_Course_Activity (
    student_id INT,
    course_id INT,
    activity_id INT,
    PRIMARY KEY (student_id, course_id, activity_id)
);

INSERT INTO Student_Course_Activity VALUES
(101, 301, 1),
(101, 301, 2),
(101, 302, 1),
(101, 302, 2),
(102, 301, 1),
(102, 301, 2),
(102, 302, 1),
(102, 302, 2);

CREATE TABLE Student_Course (
    student_id INT,
    course_id INT,
    PRIMARY KEY (student_id, course_id)
);

CREATE TABLE Student_Activity (
    student_id INT,
    activity_id INT,
    PRIMARY KEY (student_id, activity_id)
);

CREATE TABLE Course_Activity (
    course_id INT,
    activity_id INT,
    PRIMARY KEY (course_id, activity_id)
);
INSERT INTO Student_Course VALUES
(101,301),(101,302),
(102,301),(102,302);

INSERT INTO Student_Activity VALUES
(101,1),(101,2),
(102,1),(102,2);

INSERT INTO Course_Activity VALUES
(301,1),(301,2),
(302,1),(302,2);

SELECT
    sc.student_id,
    sc.course_id,
    sa.activity_id
FROM Student_Course sc
JOIN Student_Activity sa
    ON sc.student_id = sa.student_id
JOIN Course_Activity ca
    ON sc.course_id = ca.course_id
    AND sa.activity_id = ca.activity_id;