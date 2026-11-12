package src;
import java.sql.*;

public class Connectivity {

    static final String URL = "jdbc:mysql://localhost:3306/collegecourseregistrationsystem";
    static final String USER = "root";
    static final String PASSWORD = "root";

    public static void main(String[] args) {

        try {
            // 1. Establish connection
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");

            // 2. INSERT
            String insertQuery =
                    "INSERT INTO Student(student_id, student_name, email, department_id) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(insertQuery);

            ps.setInt(1, 101);
            ps.setString(2, "Rahul");
            ps.setString(3, "rahul@gmail.com");
            ps.setInt(4, 1);

            ps.executeUpdate();
            System.out.println("Student inserted successfully.");

            // 3. SELECT
            String selectQuery = "SELECT * FROM Student";

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(selectQuery);

            System.out.println("\nStudent Details:");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("student_id") + "  " +
                    rs.getString("student_name") + "  " +
                    rs.getString("email") + "  " +
                    rs.getInt("department_id")
                );
            }

            // 4. UPDATE
            String updateQuery =
                    "UPDATE Student SET email = ? WHERE student_id = ?";

            PreparedStatement ps2 = con.prepareStatement(updateQuery);

            ps2.setString(1, "rahul123@gmail.com");
            ps2.setInt(2, 101);

            ps2.executeUpdate();
            System.out.println("\nStudent email updated successfully.");

            // 5. DELETE
            String deleteQuery =
                    "DELETE FROM Student WHERE student_id = ?";

            PreparedStatement ps3 = con.prepareStatement(deleteQuery);

            ps3.setInt(1, 101);

            ps3.executeUpdate();
            System.out.println("Student deleted successfully.");

            // 6. Close connection
            rs.close();
            stmt.close();
            ps.close();
            ps2.close();
            ps3.close();
            con.close();

            System.out.println("Database connection closed.");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}