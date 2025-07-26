// File: SensorDataAPI.java
import fi.iki.elonen.NanoHTTPD;
import java.sql.*;
import java.util.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SensorDataAPI extends NanoHTTPD {

    public SensorDataAPI() throws IOException {
        super(8080); // Runs on http://localhost:8080
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("✅ Server started at http://localhost:8080/api/sensor-data");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        if (uri.equalsIgnoreCase("/api/sensor-data")) {
            Map<String, String> params = new HashMap<>();
            try {
                session.parseBody(new HashMap<>());
            } catch (Exception e) {
                e.printStackTrace();
            }
            params.putAll(session.getParms());

            String startDateStr = params.get("startDate");
            String endDateStr = params.get("endDate");

            List<Map<String, Object>> sensorData = fetchSensorData(startDateStr, endDateStr);
            String json = new Gson().toJson(sensorData);
            return newFixedLengthResponse(Response.Status.OK, "application/json", json);
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
        }
    }

    private List<Map<String, Object>> fetchSensorData(String startDateStr, String endDateStr) {
        List<Map<String, Object>> dataList = new ArrayList<>();

        String url = "jdbc:mysql://localhost:3306/greenchain_db";
        String username = "root";
        String password = "naveen0130";

        StringBuilder query = new StringBuilder("SELECT sensor_id, data_value, unit, timestamp FROM sensor_data");
        boolean hasFilter = (startDateStr != null && endDateStr != null);

        if (hasFilter) {
            query.append(" WHERE DATE(timestamp) BETWEEN ? AND ?");
        }

        query.append(" ORDER BY timestamp DESC LIMIT 100");

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = conn.prepareStatement(query.toString())) {

            if (hasFilter) {
                ps.setString(1, startDateStr);
                ps.setString(2, endDateStr);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("sensor_id", rs.getString("sensor_id"));
                row.put("data_value", rs.getDouble("data_value"));
                row.put("unit", rs.getString("unit"));
                row.put("timestamp", rs.getTimestamp("timestamp").toString());
                dataList.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    public static void main(String[] args) {
        try {
            new SensorDataAPI();
        } catch (IOException e) {
            System.err.println("❌ Failed to start API: " + e.getMessage());
        }
    }
}
