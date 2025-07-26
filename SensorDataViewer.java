// File: src/SensorDataViewer.java
import java.sql.*;
import java.util.Scanner;

public class SensorDataViewer {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:mysql://localhost:3306/greenchain_db";
        String dbUser = "root";
        String dbPassword = "naveen0130"; // 🔁 Replace with your MySQL password

        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. View all sensor data");
                System.out.println("2. Search sensor data with filters");
                System.out.println("3. Exit");
                System.out.print("Enter choice (1, 2 or 3): ");
                String choice = scanner.nextLine().trim();

                if (choice.equals("1")) {
                    viewAllData(conn);
                } else if (choice.equals("2")) {
                    searchWithFilters(conn, scanner);
                } else if (choice.equals("3")) {
                    System.out.println("Exiting program.");
                    break;
                } else {
                    System.out.println("❌ Invalid choice. Please enter 1, 2, or 3.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewAllData(Connection conn) throws SQLException {
        String sql = "SELECT s.sensor_type, s.location, d.data_value, d.unit, d.timestamp " +
                "FROM sensor_data d JOIN sensors s ON d.sensor_id = s.id";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n---- All Sensor Data ----");
            int count = 0;
            while (rs.next()) {
                displayRecord(rs);
                count++;
            }
            if (count == 0) {
                System.out.println("No data found.");
            }
        }
    }

    private static void searchWithFilters(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter sensor type (or leave blank to skip): ");
        String type = scanner.nextLine().trim();

        System.out.print("Enter location (or leave blank to skip): ");
        String location = scanner.nextLine().trim();

        System.out.print("Enter minimum data value (or leave blank to skip): ");
        String minValStr = scanner.nextLine().trim();

        Float minVal = null;
        if (!minValStr.isEmpty()) {
            try {
                minVal = Float.parseFloat(minValStr);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number entered. Skipping value filter.");
            }
        }

        StringBuilder sql = new StringBuilder(
                "SELECT s.sensor_type, s.location, d.data_value, d.unit, d.timestamp " +
                        "FROM sensor_data d JOIN sensors s ON d.sensor_id = s.id WHERE 1=1");

        if (!type.isEmpty()) {
            sql.append(" AND s.sensor_type = ?");
        }
        if (!location.isEmpty()) {
            sql.append(" AND s.location = ?");
        }
        if (minVal != null) {
            sql.append(" AND d.data_value >= ?");
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (!type.isEmpty()) stmt.setString(paramIndex++, type);
            if (!location.isEmpty()) stmt.setString(paramIndex++, location);
            if (minVal != null) stmt.setFloat(paramIndex, minVal);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n---- Filtered Sensor Data ----");
                int count = 0;
                while (rs.next()) {
                    displayRecord(rs);
                    count++;
                }
                if (count == 0) {
                    System.out.println("No data matched your filters.");
                }
            }
        }
    }

    private static void displayRecord(ResultSet rs) throws SQLException {
        String type = rs.getString("sensor_type");
        String location = rs.getString("location");
        float value = rs.getFloat("data_value");
        String unit = rs.getString("unit");
        Timestamp ts = rs.getTimestamp("timestamp");

        System.out.printf("Sensor: %s | Location: %s | Value: %.2f %s | Time: %s%n",
                type, location, value, unit, ts);
    }
}
