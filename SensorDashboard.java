// File: src/SensorDashboard.java

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SensorDashboard extends JFrame {
    private TimeSeriesCollection dataset;
    private JFreeChart chart;
    private JPanel chartPanelContainer;
    private XYPlot plot;

    public SensorDashboard() {
        setTitle("Sensor Data Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        dataset = new TimeSeriesCollection();
        chart = ChartFactory.createTimeSeriesChart(
                "Sensor Readings Over Time",
                "Time",
                "Value",
                dataset,
                true,
                true,
                false
        );

        customizeChartAppearance();

        chartPanelContainer = new JPanel(new BorderLayout());
        chartPanelContainer.setBackground(Color.DARK_GRAY);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(Color.DARK_GRAY);

        chartPanelContainer.add(chartPanel, BorderLayout.CENTER);

        JButton exportButton = new JButton("Export Chart as Image");
        exportButton.addActionListener((ActionEvent e) -> exportChart());

        JButton resetZoomButton = new JButton("Reset Zoom");
        resetZoomButton.addActionListener(e -> {
            plot.getDomainAxis().setAutoRange(true);
            plot.getRangeAxis().setAutoRange(true);
        });

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(exportButton);
        bottomPanel.add(resetZoomButton);

        add(chartPanelContainer, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshData(); // Load data initially

        // Auto-refresh every 5 seconds
        Timer timer = new Timer(5000, e -> refreshData());
        timer.start();
    }

    private void customizeChartAppearance() {
        plot = chart.getXYPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));

        plot.getDomainAxis().setAutoRange(true);
        plot.getRangeAxis().setAutoRange(true);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
        plot.setRenderer(renderer);
    }

    private void refreshData() {
        Map<Integer, TimeSeries> sensorSeriesMap = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/greenchain_db", "root", "naveen0130");
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM sensor_data ORDER BY timestamp ASC")) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int sensorId = rs.getInt("sensor_id");
                double dataValue = rs.getDouble("data_value");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                TimeSeries series = sensorSeriesMap.computeIfAbsent(sensorId,
                        id -> new TimeSeries("Sensor " + id));
                series.addOrUpdate(new Second(new Date(timestamp.getTime())), dataValue);
            }

            dataset.removeAllSeries(); // Clear old data
            for (TimeSeries series : sensorSeriesMap.values()) {
                dataset.addSeries(series);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportChart() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Chart As Image");
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                ChartUtils.saveChartAsPNG(fileToSave, chart, 1000, 600);
                JOptionPane.showMessageDialog(this, "Chart exported successfully!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting chart.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SensorDashboard dashboard = new SensorDashboard();
            dashboard.setVisible(true);
        });
    }
}
