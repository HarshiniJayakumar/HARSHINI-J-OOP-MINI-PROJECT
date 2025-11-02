import java.sql.*;

public class DBConnection {
    private static Connection con;

    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/movie_booking_db",
                    "root",
                    "harsh@123456"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}
