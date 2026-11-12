# Course Registration System (Full Stack DBMS Project)

An end-to-end College Course Registration and Academic Allotment System built with a native Java HTTP backend, JDBC, MySQL 8.0, and a responsive frontend.

---

## 🚀 Key Features

* **3 Role-Based Portals:**
  * **Student Portal:** Displays semester-specific courses in individual card boxes with real-time seat availability. Automatically allots faculty and lecture rooms upon registration.
  * **Faculty Portal:** Manages teaching sections, lists enrolled students, and handles grade submission.
  * **Administrator Portal:** Features an **Academic Allotment Dashboard** (Teachers Allocated, Rooms Allocated, Courses Allocated) plus full master table CRUD management.
* **Full Curriculum Coverage:**
  * 6 Engineering Departments (AI, CSE, ECE, MECH, IT, BIOTECH).
  * 8 Academic Semesters (Semesters 1 through 8).
  * 240 distinct courses with pre-configured Course Offerings and Sections.

---

## 🛠️ Tech Stack

* **Frontend:** HTML5, CSS3 (Modern Academic UI), Vanilla JavaScript (Fetch API)
* **Backend:** Java 17+ (Built-in `com.sun.net.httpserver`, JDBC)
* **Database:** MySQL 8.0 (InnoDB Storage Engine)

---

## 🗄️ Database Architecture (9 Tables)

1. `Department` – Academic departments
2. `Student` – Student directory with department, academic year, and current semester
3. `Faculty` – Professors and instructors mapped to departments
4. `Course` – Curriculum subjects with credits, department, year, and semester
5. `Semester` – Academic terms (Semesters 1 to 8)
6. `Classroom` – Lecture halls with maximum seating capacity
7. `Course_Offering` – Maps courses to specific semesters
8. `Course_Section` – Maps offerings to faculty instructors and classroom venues
9. `Enrollment` – Student registrations, status, and letter grades

---

## ⚡ How to Run Locally

### 1. Database Setup
Ensure MySQL is running on port 3306 with database name `collegecourseregistrationsystem`:
```sql
SOURCE database/ddl.sql;
SOURCE database/dml.sql;
SOURCE database/procedures.sql;
```

### 2. Start the Backend Server
Run `src/Server.java` from VS Code, or compile and run via command line:
```bash
javac -cp "lib/*" -d bin src/Server.java
java -cp "lib/*;bin" src.Server
```

### 3. Open in Browser
Visit: **`http://localhost:8080`**

---

## 🔑 Demo Login Credentials

* **Administrator:** `admin@college.edu` / `admin123`
* **Student (Sreenidhi):** `sreenidhi@college.edu` / `pass123`
* **Faculty (Dr. Rajesh):** `rajesh201@univ.edu` / `pass123`
