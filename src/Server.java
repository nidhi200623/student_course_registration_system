package src;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/*
 * Backend of the College Course Registration System.
 * Uses Java's built-in HTTP server + JDBC (MySQL).
 * Directly maps to original database tables without modifying any schema:
 * Department, Student, Faculty, Course, Semester, Classroom, Course_Offering, Course_Section, Enrollment.
 */
public class Server {

    static final String URL = "jdbc:mysql://localhost:3306/collegecourseregistrationsystem";
    static final String USER = "root";
    static final String PASSWORD = "root";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/", Server::handleApi);
        server.createContext("/", Server::handleFile);
        server.start();
        System.out.println("Server started. Open http://localhost:8080 in your browser");
    }

    // ------------------------------------------------------------------
    // API requests
    // ------------------------------------------------------------------
    static void handleApi(HttpExchange ex) throws IOException {
        Map<String, String> p = readParams(ex);
        String path = ex.getRequestURI().getPath();
        String result;

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            result = switch (path) {
                case "/api/login" -> login(con, p);

                // ---------- STUDENT ----------
                case "/api/semesters" -> query(con,
                    "SELECT semester_id, semester_name, academic_year FROM Semester ORDER BY semester_id");

                case "/api/student/available" -> query(con,
                    "SELECT cs.section_id, c.course_name, c.credits, c.year, c.semester, s.semester_name, s.academic_year, " +
                    "f.faculty_name, cl.room_number, cl.capacity, " +
                    "(SELECT COUNT(*) FROM Enrollment e WHERE e.section_id = cs.section_id) AS enrolled " +
                    "FROM Course_Section cs " +
                    "JOIN Course_Offering co ON cs.offering_id = co.offering_id " +
                    "JOIN Course c ON co.course_id = c.course_id " +
                    "JOIN Semester s ON co.semester_id = s.semester_id " +
                    "JOIN Faculty f ON cs.faculty_id = f.faculty_id " +
                    "JOIN Classroom cl ON cs.classroom_id = cl.classroom_id " +
                    "JOIN Student st ON st.student_id = ? " +
                    "WHERE c.department_id = st.department_id AND c.year = st.year AND c.semester = st.semester " +
                    "ORDER BY c.course_name", p.get("id"));

                case "/api/student/my" -> query(con,
                    "SELECT e.enrollment_id, c.course_name, c.credits, f.faculty_name, " +
                    "cl.room_number, s.semester_name, e.enrollment_date, e.status, e.grade " +
                    "FROM Enrollment e " +
                    "JOIN Course_Section cs ON e.section_id = cs.section_id " +
                    "JOIN Course_Offering co ON cs.offering_id = co.offering_id " +
                    "JOIN Course c ON co.course_id = c.course_id " +
                    "JOIN Semester s ON co.semester_id = s.semester_id " +
                    "JOIN Faculty f ON cs.faculty_id = f.faculty_id " +
                    "JOIN Classroom cl ON cs.classroom_id = cl.classroom_id " +
                    "WHERE e.student_id = ?", p.get("id"));

                case "/api/student/register" -> register(con, p.get("id"), p.get("section_id"));

                case "/api/student/drop" -> message(update(con,
                    "DELETE FROM Enrollment WHERE enrollment_id = ? AND student_id = ? AND grade IS NULL",
                    p.get("enrollment_id"), p.get("id")), "Course dropped");

                // ---------- FACULTY ----------
                case "/api/faculty/sections" -> query(con,
                    "SELECT cs.section_id, c.course_name, c.credits, s.semester_name, cl.room_number, " +
                    "(SELECT COUNT(*) FROM Enrollment e WHERE e.section_id = cs.section_id) AS enrolled " +
                    "FROM Course_Section cs " +
                    "JOIN Course_Offering co ON cs.offering_id = co.offering_id " +
                    "JOIN Course c ON co.course_id = c.course_id " +
                    "JOIN Semester s ON co.semester_id = s.semester_id " +
                    "JOIN Classroom cl ON cs.classroom_id = cl.classroom_id " +
                    "WHERE cs.faculty_id = ?", p.get("id"));

                case "/api/faculty/students" -> query(con,
                    "SELECT e.enrollment_id, st.student_id, st.student_name, st.email, " +
                    "d.department_name, e.status, e.grade " +
                    "FROM Enrollment e " +
                    "JOIN Student st ON e.student_id = st.student_id " +
                    "LEFT JOIN Department d ON st.department_id = d.department_id " +
                    "JOIN Course_Section cs ON e.section_id = cs.section_id " +
                    "WHERE e.section_id = ? AND cs.faculty_id = ?", p.get("section_id"), p.get("id"));

                case "/api/faculty/grade" -> message(update(con,
                    "UPDATE Enrollment e JOIN Course_Section cs ON e.section_id = cs.section_id " +
                    "SET e.grade = ?, e.status = 'Completed' " +
                    "WHERE e.enrollment_id = ? AND cs.faculty_id = ?",
                    p.get("grade"), p.get("enrollment_id"), p.get("id")), "Grade saved");

                // ---------- ADMINISTRATOR ----------
                case "/api/admin/dashboard-stats" -> getDashboardStats(con);

                case "/api/admin/allotment-teachers" -> query(con,
                    "SELECT f.faculty_id, f.faculty_name, COALESCE(d.department_name, 'General') AS department_name, " +
                    "c.course_name, s.semester_name, cs.section_id, cl.room_number, " +
                    "(SELECT COUNT(*) FROM Enrollment e WHERE e.section_id = cs.section_id) AS enrolled_students " +
                    "FROM Course_Section cs " +
                    "JOIN Faculty f ON cs.faculty_id = f.faculty_id " +
                    "LEFT JOIN Department d ON f.department_id = d.department_id " +
                    "JOIN Course_Offering co ON cs.offering_id = co.offering_id " +
                    "JOIN Course c ON co.course_id = c.course_id " +
                    "JOIN Semester s ON co.semester_id = s.semester_id " +
                    "JOIN Classroom cl ON cs.classroom_id = cl.classroom_id " +
                    "ORDER BY f.faculty_name, c.course_name");

                case "/api/admin/allotment-rooms" -> query(con,
                    "SELECT cl.classroom_id, cl.room_number, cl.capacity, " +
                    "COALESCE(cs.section_id, 0) AS section_id, " +
                    "COALESCE(c.course_name, 'Unallotted') AS course_name, " +
                    "COALESCE(f.faculty_name, 'None') AS faculty_name, " +
                    "COALESCE(s.semester_name, '-') AS semester_name, " +
                    "(SELECT COUNT(*) FROM Enrollment e WHERE e.section_id = cs.section_id) AS current_enrolled " +
                    "FROM Classroom cl " +
                    "LEFT JOIN Course_Section cs ON cl.classroom_id = cs.classroom_id " +
                    "LEFT JOIN Course_Offering co ON cs.offering_id = co.offering_id " +
                    "LEFT JOIN Course c ON co.course_id = c.course_id " +
                    "LEFT JOIN Faculty f ON cs.faculty_id = f.faculty_id " +
                    "LEFT JOIN Semester s ON co.semester_id = s.semester_id " +
                    "ORDER BY cl.room_number");

                case "/api/admin/allotment-courses" -> query(con,
                    "SELECT cs.section_id, c.course_id, c.course_name, c.year, c.semester, c.credits, " +
                    "COALESCE(d.department_name, 'General') AS department_name, " +
                    "f.faculty_name AS allocated_faculty, cl.room_number AS allocated_room, " +
                    "cl.capacity, (SELECT COUNT(*) FROM Enrollment e WHERE e.section_id = cs.section_id) AS enrolled " +
                    "FROM Course_Section cs " +
                    "JOIN Course_Offering co ON cs.offering_id = co.offering_id " +
                    "JOIN Course c ON co.course_id = c.course_id " +
                    "LEFT JOIN Department d ON c.department_id = d.department_id " +
                    "JOIN Faculty f ON cs.faculty_id = f.faculty_id " +
                    "JOIN Classroom cl ON cs.classroom_id = cl.classroom_id " +
                    "ORDER BY c.year, c.semester, c.course_name");

                case "/api/admin/list" -> query(con, LIST.get(p.get("type")));

                case "/api/admin/add" -> {
                    String[] add = ADD.get(p.get("type"));
                    String[] fields = add[1].split(",");
                    Object[] values = new Object[fields.length];
                    for (int i = 0; i < fields.length; i++) values[i] = p.get(fields[i]);
                    yield message(update(con, add[0], values), "Added successfully");
                }

                case "/api/admin/delete" -> message(update(con, DELETE.get(p.get("type")), p.get("id")),
                    "Deleted successfully");

                default -> error("Unknown request");
            };
        } catch (SQLIntegrityConstraintViolationException e) {
            result = error("Not allowed: " + e.getMessage());
        } catch (Exception e) {
            result = error(e.getMessage());
        }
        send(ex, result, "application/json");
    }

    // Login for all three roles without modifying tables
    static String login(Connection con, Map<String, String> p) throws SQLException {
        String role = p.getOrDefault("role", "student");
        String email = p.getOrDefault("email", "").trim();
        String password = p.getOrDefault("password", "").trim();

        if (email.isEmpty()) {
            return error("Email is required");
        }

        if ("admin".equalsIgnoreCase(role)) {
            // Administrator login
            if (email.equalsIgnoreCase("admin@college.edu") && ("admin123".equals(password) || password.isEmpty())) {
                return "[{\"id\":\"1\",\"name\":\"Administrator\"}]";
            }
            return error("Invalid admin email or password");
        }

        // Student or Faculty lookup by email from original tables
        String sql = "student".equalsIgnoreCase(role)
            ? "SELECT student_id AS id, student_name AS name, year, semester FROM Student WHERE email = ?"
            : "SELECT faculty_id AS id, faculty_name AS name FROM Faculty WHERE email = ?";

        String rows = query(con, sql, email);
        return rows.equals("[]") ? error("No " + role + " found with email: " + email) : rows;
    }

    // Student registers for a section: 100% of Concurrency Control & Locking is executed inside MySQL!
    static String register(Connection con, String studentId, String sectionId) {
        String sql = "{CALL RegisterStudentConcurrently(?, ?, ?)}";
        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, Integer.parseInt(studentId));
            cs.setInt(2, Integer.parseInt(sectionId));
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.execute();
            String msg = cs.getString(3);
            return message(1, msg != null ? msg : "Registered successfully");
        } catch (SQLException e) {
            return error(e.getMessage());
        }
    }

    // Returns live stats for the Academic Allotment Dashboard
    static String getDashboardStats(Connection con) throws SQLException {
        int teachers = count(con, "SELECT COUNT(DISTINCT faculty_id) FROM Course_Section");
        int rooms = count(con, "SELECT COUNT(DISTINCT classroom_id) FROM Course_Section");
        int courses = count(con, "SELECT COUNT(*) FROM Course_Section");
        int totalStudents = count(con, "SELECT COUNT(*) FROM Student");
        int totalEnrollments = count(con, "SELECT COUNT(*) FROM Enrollment");
        return String.format(
            "{\"teachers\":%d,\"rooms\":%d,\"courses\":%d,\"students\":%d,\"enrollments\":%d}",
            teachers, rooms, courses, totalStudents, totalEnrollments
        );
    }

    // ---------- Admin SQL (with year and semester columns) ----------
    static final Map<String, String> LIST = Map.of(
        "departments", "SELECT department_id, department_name FROM Department",
        "students", "SELECT s.student_id, s.student_name, s.email, s.year, s.semester, d.department_name " +
                    "FROM Student s LEFT JOIN Department d ON s.department_id = d.department_id",
        "faculty", "SELECT f.faculty_id, f.faculty_name, f.email, d.department_name " +
                   "FROM Faculty f LEFT JOIN Department d ON f.department_id = d.department_id",
        "courses", "SELECT c.course_id, c.course_name, c.credits, c.year, c.semester, d.department_name " +
                   "FROM Course c LEFT JOIN Department d ON c.department_id = d.department_id",
        "semesters", "SELECT semester_id, semester_name, academic_year FROM Semester",
        "classrooms", "SELECT classroom_id, room_number, capacity FROM Classroom",
        "offerings", "SELECT co.offering_id, c.course_name, s.semester_name, s.academic_year " +
                     "FROM Course_Offering co JOIN Course c ON co.course_id = c.course_id " +
                     "JOIN Semester s ON co.semester_id = s.semester_id",
        "sections", "SELECT cs.section_id, c.course_name, s.semester_name, f.faculty_name, cl.room_number " +
                    "FROM Course_Section cs JOIN Course_Offering co ON cs.offering_id = co.offering_id " +
                    "JOIN Course c ON co.course_id = c.course_id JOIN Semester s ON co.semester_id = s.semester_id " +
                    "JOIN Faculty f ON cs.faculty_id = f.faculty_id JOIN Classroom cl ON cs.classroom_id = cl.classroom_id"
    );

    // { insert SQL, form field names matching table definitions }
    static final Map<String, String[]> ADD = Map.of(
        "departments", new String[]{"INSERT INTO Department VALUES (?, ?)", "department_id,department_name"},
        "students", new String[]{"INSERT INTO Student (student_id, student_name, email, department_id, year, semester) VALUES (?, ?, ?, ?, ?, ?)",
                                 "student_id,student_name,email,department_id,year,semester"},
        "faculty", new String[]{"INSERT INTO Faculty (faculty_id, faculty_name, email, department_id) VALUES (?, ?, ?, ?)",
                                "faculty_id,faculty_name,email,department_id"},
        "courses", new String[]{"INSERT INTO Course (course_id, course_name, credits, department_id, year, semester) VALUES (?, ?, ?, ?, ?, ?)",
                                "course_id,course_name,credits,department_id,year,semester"},
        "semesters", new String[]{"INSERT INTO Semester VALUES (?, ?, ?)", "semester_id,semester_name,academic_year"},
        "classrooms", new String[]{"INSERT INTO Classroom VALUES (?, ?, ?)", "classroom_id,room_number,capacity"},
        "offerings", new String[]{"INSERT INTO Course_Offering VALUES (?, ?, ?)", "offering_id,course_id,semester_id"},
        "sections", new String[]{"INSERT INTO Course_Section VALUES (?, ?, ?, ?)", "section_id,offering_id,faculty_id,classroom_id"}
    );

    static final Map<String, String> DELETE = Map.of(
        "departments", "DELETE FROM Department WHERE department_id = ?",
        "students", "DELETE FROM Student WHERE student_id = ?",
        "faculty", "DELETE FROM Faculty WHERE faculty_id = ?",
        "courses", "DELETE FROM Course WHERE course_id = ?",
        "semesters", "DELETE FROM Semester WHERE semester_id = ?",
        "classrooms", "DELETE FROM Classroom WHERE classroom_id = ?",
        "offerings", "DELETE FROM Course_Offering WHERE offering_id = ?",
        "sections", "DELETE FROM Course_Section WHERE section_id = ?"
    );

    // ------------------------------------------------------------------
    // JDBC helpers
    // ------------------------------------------------------------------
    static PreparedStatement prepare(Connection con, String sql, Object... params) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            Object v = params[i];
            ps.setObject(i + 1, (v == null || v.toString().isEmpty()) ? null : v);
        }
        return ps;
    }

    // SELECT -> JSON array
    static String query(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(con, sql, params); ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            StringBuilder json = new StringBuilder("[");
            while (rs.next()) {
                if (json.length() > 1) json.append(",");
                json.append("{");
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    if (i > 1) json.append(",");
                    String value = rs.getString(i);
                    json.append("\"").append(md.getColumnLabel(i)).append("\":")
                        .append(value == null ? "null" : "\"" + escape(value) + "\"");
                }
                json.append("}");
            }
            return json.append("]").toString();
        }
    }

    // INSERT / UPDATE / DELETE -> number of rows changed
    static int update(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(con, sql, params)) {
            return ps.executeUpdate();
        }
    }

    // SELECT that returns a single number
    static int count(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = prepare(con, sql, params); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    static String message(int rows, String text) {
        return rows > 0 ? "{\"message\":\"" + text + "\"}" : error("No record changed");
    }

    static String error(String text) {
        return "{\"error\":\"" + escape(text) + "\"}";
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    // ------------------------------------------------------------------
    // HTTP helpers
    // ------------------------------------------------------------------
    static Map<String, String> readParams(HttpExchange ex) throws IOException {
        Map<String, String> map = new HashMap<>();
        String q = ex.getRequestURI().getRawQuery();
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        for (String part : ((q == null ? "" : q) + "&" + body).split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    // Serves static files
    static void handleFile(HttpExchange ex) throws IOException {
        String name = ex.getRequestURI().getPath().substring(1);
        if (name.isEmpty()) name = "index.html";
        boolean allowed = !name.contains("..") && (name.endsWith(".html") || name.endsWith(".css") || name.endsWith(".js"));

        if (!allowed) {
            send(ex, "Page not found", "text/plain");
            return;
        }

        Path file = resolveFile(name);
        if (file == null || !Files.exists(file)) {
            send(ex, "Page not found", "text/plain");
            return;
        }
        String type = name.endsWith(".css") ? "text/css" : name.endsWith(".js") ? "application/javascript" : "text/html";
        send(ex, Files.readString(file), type);
    }

    // Resolves file location dynamically regardless of whether Java was started from the root or subfolder
    static Path resolveFile(String name) {
        String cleanName = name.startsWith("html/") ? name.substring(5) : name;

        String[] bases = {
            "",
            "html/",
            "college_course_registration_frontend/",
            "college_course_registration_frontend/html/",
            "c:/Users/sreen/Downloads/college_course_registration_frontend/",
            "c:/Users/sreen/Downloads/college_course_registration_frontend/college_course_registration_frontend/",
            "c:/Users/sreen/Downloads/college_course_registration_frontend/college_course_registration_frontend/html/"
        };

        for (String base : bases) {
            Path p1 = Path.of(base + name);
            if (Files.exists(p1) && !Files.isDirectory(p1)) return p1;

            Path p2 = Path.of(base + cleanName);
            if (Files.exists(p2) && !Files.isDirectory(p2)) return p2;
        }

        try {
            Path codeLoc = Path.of(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path curr = codeLoc;
            for (int i = 0; i < 5 && curr != null; i++) {
                Path direct = curr.resolve(name);
                if (Files.exists(direct) && !Files.isDirectory(direct)) return direct;
                Path nested = curr.resolve("college_course_registration_frontend").resolve(name);
                if (Files.exists(nested) && !Files.isDirectory(nested)) return nested;
                Path htmlNested = curr.resolve("html").resolve(name);
                if (Files.exists(htmlNested) && !Files.isDirectory(htmlNested)) return htmlNested;
                curr = curr.getParent();
            }
        } catch (Exception ignored) {}

        return null;
    }

    static void send(HttpExchange ex, String text, String type) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=UTF-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
