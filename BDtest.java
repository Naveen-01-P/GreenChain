import java.sql.Connection;
import java.sql.DriverManager;

public class BDtest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/greenchain_db";
        String username = "root";
        String password = "naveen0130"; // your actual password

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connected to MySQL!");
        } catch (Exception e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }
    }
}
