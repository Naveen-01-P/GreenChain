import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.sql.*;
import java.text.SimpleDateFormat;

public class SensorDataService {

    public static XYDataset getDataset(String startDateStr, String endDateStr) {
        TimeSeries series = new TimeSeries("Sensor Value");

        String url = "jdbc:mysql://localhost:3306/greenchain_db";
        String user = "root";
        String password = "naveen0130";

        String query = "SELECT timestamp, data_value FROM sensor_data";
        if (startDateStr != null && endDateStr != null) {
            query += " WHERE timestamp BETWEEN ? AND ?";
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (startDateStr != null && endDateStr != null) {
                stmt.setString(1, startDateStr);
                stmt.setString(2, endDateStr);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("timestamp");
                double value = rs.getDouble("data_value");

                series.addOrUpdate(new Millisecond(timestamp), value);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }
}
