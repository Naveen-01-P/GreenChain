import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;

import java.io.File;
import java.io.IOException;

public class ChartExporter {
    public static void exportAsImage(JFreeChart chart, String fileName) {
        try {
            File file = new File(fileName);
            ChartUtils.saveChartAsPNG(file, chart, 800, 600);
            System.out.println("Chart exported to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
