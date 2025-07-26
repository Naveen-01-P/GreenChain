// File: src/SensorDataInserter.java
import java.sql.*;
import java.util.Scanner;

public class SensorDataInserter {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:mysql://localhost:3306/greenchain_db";
        String dbUser = "root";
        String dbPassword = "naveen0130"; // change if needed

        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. View all sensors");
                System.out.println("2. Insert new sensor");
                System.out.println("3. Insert sensor data");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        viewSensors(conn);
                        break;
                    case "2":
                        insertSensor(conn, scanner);
                        break;
                    case "3":
                        insertSensorData(conn, scanner);
                        break;
                    case "4":
                        System.out.println("Exiting.");
                        return;
                    default:
                        System.out.println("❌ Invalid choice.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewSensors(Connection conn) throws SQLException {
        String sql = "SELECT id, sensor_type, location FROM sensors";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("\n📟 Registered Sensors:");
            int count = 0;
            while (rs.next()) {
                System.out.printf("ID: %d | Type: %s | Location: %s%n",
                        rs.getInt("id"), rs.getString("sensor_type"), rs.getString("location"));
                count++;
            }
            if (count == 0) {
                System.out.println("No sensors found.");
            }
        }
    }

    private static void insertSensor(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter sensor type: ");
        String type = scanner.nextLine();
        System.out.print("Enter location: ");
        String location = scanner.nextLine();

        String checkSql = "SELECT id FROM sensors WHERE sensor_type = ? AND location = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, type);
            checkStmt.setString(2, location);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("⚠️ Sensor already exists with ID: " + rs.getInt("id"));
                return;
            }
        }

        String insertSql = "INSERT INTO sensors (sensor_type, location) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, type);
            insertStmt.setString(2, location);
            insertStmt.executeUpdate();
            System.out.println("✅ Sensor inserted successfully.");
        }
    }

    private static void insertSensorData(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter sensor ID: ");
        int sensorId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter data value: ");
        float value = Float.parseFloat(scanner.nextLine());

        System.out.print("Enter unit (e.g., °C, ppm, %): ");
        String unit = scanner.nextLine();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        String sql = "INSERT INTO sensor_data (sensor_id, data_value, unit, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sensorId);
            stmt.setFloat(2, value);
            stmt.setString(3, unit);
            stmt.setTimestamp(4, timestamp);
            stmt.executeUpdate();
            System.out.println("✅ Sensor data inserted successfully.");
        }
    }
}
