import java.sql.*;
import java.util.ResourceBundle;

public class DbChecker {
    public static void main(String[] args) throws Exception {
        ResourceBundle props = ResourceBundle.getBundle("dbconfig");
        Connection conn = DriverManager.getConnection(props.getString("db.url"), props.getString("db.user"), props.getString("db.password"));
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DISTINCT status FROM Loans");
        while (rs.next()) {
            System.out.println("Status in DB: [" + rs.getString(1) + "]");
        }
        conn.close();
    }
}
