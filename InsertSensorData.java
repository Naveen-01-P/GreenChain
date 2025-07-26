import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class InsertSensorData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/greenchain_db";
        String username = "root";
        String password = "naveen0130"; // 🔁 Replace with your real password

        Scanner scanner = new Scanner(System.in);

        try {
            // 1. Connect to DB
            Connection conn = DriverManager.getConnection(url, username, password);

            // 2. Take input
            System.out.print("Enter Sensor Name: ");
            String sensorName = scanner.nextLine();

            System.out.print("Enter Location: ");
            String location = scanner.nextLine();

            System.out.print("Enter Reading: ");
            double reading = scanner.nextDouble();

            // 3. Prepare SQL
            String query = "INSERT INTO SensorData (sensor_name, location, reading, timestamp) VALUES (?, ?, ?, NOW())";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, sensorName);
            pstmt.setString(2, location);
            pstmt.setDouble(3, reading);

            // 4. Execute
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Sensor data inserted successfully!");
            } else {
                System.out.println("❌ Insertion failed.");
            }

            // 5. Cleanup
            pstmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Error inserting data:");
            e.printStackTrace();
        }
    }
}
