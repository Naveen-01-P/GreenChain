import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReadSensorData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/greenchain_db";
        String username = "root";
        String password = "naveen0130"; // Change it

        String selectSQL = "SELECT * FROM SensorData";

        try (
                Connection conn = DriverManager.getConnection(url, username, password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectSQL)
        ) {
            System.out.println("✅ Sensor Data:\n");

            while (rs.next()) {
                int id = rs.getInt("id");
                String sensorName = rs.getString("sensor_name");
                String location = rs.getString("location");
                double readingValue = rs.getDouble("reading_value");
                String timestamp = rs.getString("timestamp");

                System.out.println("ID: " + id);
                System.out.println("Sensor: " + sensorName);
                System.out.println("Location: " + location);
                System.out.println("Reading: " + readingValue);
                System.out.println("Time: " + timestamp);
                System.out.println("---------------------------");
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to fetch sensor data!");
            e.printStackTrace();
        }
    }
}
