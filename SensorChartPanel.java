import javafx.scene.Node;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;

public class SensorChartPanel {
    private JFreeChart chart;
    private ChartViewer chartViewer;

    public SensorChartPanel() {
        chart = createChart(); // Initial dummy chart
        chartViewer = new ChartViewer(chart);
    }

    private JFreeChart createChart() {
        XYDataset dataset = SensorDataService.getDataset(null, null); // Adjust based on your data service
        return ChartFactory.createTimeSeriesChart(
                "Sensor Data",
                "Timestamp",
                "Value",
                dataset,
                true,
                true,
                false
        );
    }

    public Node getChartNode() {
        return chartViewer;
    }

    public JFreeChart getChart() {
        return chart;
    }

    public void updateChart(String startDate, String endDate) {
        XYDataset dataset = SensorDataService.getDataset(startDate, endDate);
        chart.getXYPlot().setDataset(dataset);
    }
}
