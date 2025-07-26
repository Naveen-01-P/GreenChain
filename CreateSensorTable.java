import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateSensorTable {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/greenchain_db";
        String username = "root";
        String password = "naveen0130"; // Replace with your MySQL password

        String createTableSQL = "CREATE TABLE IF NOT EXISTS SensorData (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "sensor_name VARCHAR(100)," +
                "location VARCHAR(100)," +
                "reading_value DOUBLE," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createTableSQL);
            System.out.println("✅ Table 'SensorData' created successfully!");
        } catch (Exception e) {
            System.out.println("❌ Failed to create table!");
            e.printStackTrace();
        }
    }
}
