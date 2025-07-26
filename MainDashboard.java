// File: MainDashboard.java

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class MainDashboard extends Application {

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private VBox rootLayout;
    private BorderPane chartContainer;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private SensorChartPanel chartPanel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GreenChain - Sensor Data Dashboard");

        // Date Pickers
        startDatePicker = new DatePicker();
        endDatePicker = new DatePicker();
        setDatePickerFormat(startDatePicker);
        setDatePickerFormat(endDatePicker);

        Button filterButton = new Button("Filter");
        Button exportButton = new Button("Export as PNG");

        HBox controlBar = new HBox(10, new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker,
                filterButton, exportButton);
        controlBar.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Chart Panel Placeholder
        chartContainer = new BorderPane();
        chartContainer.setPrefHeight(500);

        chartPanel = new SensorChartPanel();
        chartContainer.setCenter(chartPanel.getChartNode());

        // Root Layout
        rootLayout = new VBox(10, controlBar, chartContainer);
        Scene scene = new Scene(rootLayout, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Button Actions
        filterButton.setOnAction(e -> loadData());
        exportButton.setOnAction(e -> ChartExporter.exportAsImage(chartPanel.getChart(), "sensor_chart.png"));

        // Auto-refresh every 10 seconds
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                javafx.application.Platform.runLater(() -> loadData());
            }
        }, 0, 10000); // every 10 seconds
    }

    private void loadData() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        String startStr = (start != null) ? start.format(formatter) : "";
        String endStr = (end != null) ? end.format(formatter) : "";

        chartPanel.updateChart(startStr, endStr);
    }

    private void setDatePickerFormat(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
