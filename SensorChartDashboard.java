import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import javax.swing.Timer;
import java.util.Date;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class SensorChartDashboard extends JFrame {

    private JComboBox<String> cityComboBox;
    private JComboBox<String> sensorTypeComboBox;
    private JPanel chartContainer;
    private Timer timer;

    private final String DB_URL = "jdbc:mysql://localhost:3306/greenchain_db";
    private final String DB_USER = "root";
    private final String DB_PASS = "naveen0130";

    public SensorChartDashboard() {
        setTitle("GreenChain Sensor Dashboard");
        setSize(1000, 600);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Top Panel with Dropdowns
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cityComboBox = new JComboBox<>();
        sensorTypeComboBox = new JComboBox<>();
        filterPanel.add(new JLabel("City:"));
        filterPanel.add(cityComboBox);
        filterPanel.add(new JLabel("Sensor Type:"));
        filterPanel.add(sensorTypeComboBox);
        add(filterPanel, BorderLayout.NORTH);

        // Chart Panel
        chartContainer = new JPanel(new BorderLayout());
        add(chartContainer, BorderLayout.CENTER);

        loadFilterOptions();

        cityComboBox.addActionListener(e -> updateChart());
        sensorTypeComboBox.addActionListener(e -> updateChart());

        // Initial chart update
        updateChart();

        // Timer to auto-refresh every 2 seconds
        timer = new Timer(2000, e -> updateChart());
        timer.start();

        setVisible(true);
    }

    private void loadFilterOptions() {
        Set<String> cities = new HashSet<>();
        Set<String> sensorTypes = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement("SELECT sensor_id FROM sensor_data");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String sensorId = rs.getString("sensor_id");
                String[] parts = sensorId.split("_");
                if (parts.length >= 3) {
                    cities.add(parts[0]);
                    sensorTypes.add(parts[1]);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<String> sortedCities = new ArrayList<>(cities);
        Collections.sort(sortedCities);
        cityComboBox.addItem("All");
        for (String city : sortedCities) cityComboBox.addItem(city);

        List<String> sortedTypes = new ArrayList<>(sensorTypes);
        Collections.sort(sortedTypes);
        sensorTypeComboBox.addItem("All");
        for (String type : sortedTypes) sensorTypeComboBox.addItem(type);
    }

    private void updateChart() {
        String selectedCity = (String) cityComboBox.getSelectedItem();
        String selectedType = (String) sensorTypeComboBox.getSelectedItem();

        TimeSeries series = new TimeSeries("Sensor Data");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String query = "SELECT sensor_id, data_value, timestamp FROM sensor_data";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String sensorId = rs.getString("sensor_id");
                double value = rs.getDouble("data_value");
                Timestamp ts = rs.getTimestamp("timestamp");

                String[] parts = sensorId.split("_");
                if (parts.length >= 3) {
                    String city = parts[0];
                    String type = parts[1];

                    boolean matchCity = selectedCity.equals("All") || city.equals(selectedCity);
                    boolean matchType = selectedType.equals("All") || type.equals(selectedType);

                    if (matchCity && matchType) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(ts);
                        Second time = new Second(cal.getTime());
                        series.addOrUpdate(time, value);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Live Sensor Data",
                "Timestamp",
                "Value",
                dataset,
                false,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setDefaultToolTipGenerator((xyDataset, seriesIndex, itemIndex) -> {
            Number y = xyDataset.getY(seriesIndex, itemIndex);
            Number x = xyDataset.getX(seriesIndex, itemIndex);
            Date date = new Date(x.longValue());
            return new SimpleDateFormat("HH:mm:ss").format(date) + " = " + y;
        });
        plot.setRenderer(renderer);

        DateAxis domain = (DateAxis) plot.getDomainAxis();
        domain.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

        chartContainer.removeAll();
        chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SensorChartDashboard::new);
    }
}
