import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AutoGenerateSensor {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/greenchain_db";
    private static final String USER = "root";
    private static final String PASSWORD = "naveen0130";

    private static final List<String> CITIES = Arrays.asList("Chennai", "Mumbai", "Bangalore", "Delhi", "Hyderabad");
    private static final List<String> SENSOR_TYPES = Arrays.asList("TEM", "HUM", "CO2", "WIN");

    private static final Map<String, Float> lastValues = new HashMap<>();
    private static final Random random = new Random();
    private static int sensorIdCounter = 1000;

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            while (true) {
                for (String city : CITIES) {
                    for (String type : SENSOR_TYPES) {
                        int batchSize = 20 + random.nextInt(11); // 20–30
                        for (int i = 0; i < batchSize; i++) {
                            insertSensorData(conn, city, type);
                            Thread.sleep(2); // ~0.002 seconds
                        }
                    }
                }
                deleteOldData(conn); // keep only latest 150
                Thread.sleep(2000); // 2 seconds gap before cycling again
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertSensorData(Connection conn, String city, String type) throws SQLException {
        String sensorId = city.substring(0, 3).toUpperCase() + "_" + type + "_" + sensorIdCounter++;
        float value = generateValue(type, city);
        String unit = getUnit(type);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String sql = "INSERT INTO sensor_data (sensor_id, data_value, unit, timestamp, city) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sensorId);
            stmt.setFloat(2, value);
            stmt.setString(3, unit);
            stmt.setString(4, timestamp);
            stmt.setString(5, city);
            stmt.executeUpdate();

            System.out.printf("Inserted | %s | %s | %.2f %s | %s\n", city, sensorId, value, unit, timestamp);
        }
    }

    private static float generateValue(String type, String city) {
        String key = city + "_" + type;
        float prev = lastValues.getOrDefault(key, getInitialValue(type));
        float next = prev;

        switch (type) {
            case "TEM":
                next = prev + (random.nextBoolean() ? 0.2f : -0.2f);
                next = clamp(next, 20f, 40f);
                break;
            case "HUM":
                double t = System.currentTimeMillis() / 1000.0;
                next = (float)(60 + 20 * Math.sin(t / 5));
                break;
            case "CO2":
                next = prev + (random.nextInt(10) == 0 ? random.nextFloat() * 200 : random.nextFloat() * 5 - 2);
                next = clamp(next, 300f, 1000f);
                break;
            case "WIN":
                next = (float)(10 + 5 * Math.sin(System.currentTimeMillis() / 1000.0));
                break;
        }

        lastValues.put(key, next);
        return next;
    }

    private static float getInitialValue(String type) {
        switch (type) {
            case "TEM": return 25f;
            case "HUM": return 60f;
            case "CO2": return 400f;
            case "WIN": return 10f;
            default: return 0f;
        }
    }

    private static String getUnit(String type) {
        switch (type) {
            case "TEM": return "°C";
            case "HUM": return "%";
            case "CO2": return "ppm";
            case "WIN": return "km/h";
            default: return "";
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void deleteOldData(Connection conn) throws SQLException {
        String sql = "DELETE FROM sensor_data WHERE id NOT IN (SELECT id FROM (SELECT id FROM sensor_data ORDER BY timestamp DESC LIMIT 10000) temp)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Deleted old entries: " + rowsDeleted);
            }
        }
    }
}
