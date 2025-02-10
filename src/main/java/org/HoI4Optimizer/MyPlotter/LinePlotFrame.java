package org.HoI4Optimizer.MyPlotter;
import java.awt.geom.AffineTransform;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeTableXYDataset;
import java.awt.Font;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Calendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;

/// Class which contains some kind of line-plot of data
public class LinePlotFrame extends JFrame
{
    public static int defaultWidth = 2400;
    public static int defaultHeight = 1200;

    public LinePlotFrame(String name, String yAxisName, List<LinePlotData> lines, DataLogger.logDataType type, boolean stackedAreas, boolean save)
    {
        JFreeChart chart;
        double isPercent = type == DataLogger.logDataType.Percent || type == DataLogger.logDataType.UnboundedPercent || type == DataLogger.logDataType.PositivePercent || type == DataLogger.logDataType.PositiveUnboundedPercent ? 100.0 : 1.0;
        XYPlot plot;
        if (stackedAreas)
        {
            var dataset = new TimeTableXYDataset();

            chart = ChartFactory.createStackedXYAreaChart(
                    name, "Day", yAxisName,
                    dataset, PlotOrientation.VERTICAL, true, true, false
            );

            plot = chart.getXYPlot();
            var renderer = new XYStepRenderer();

            for (int i = 0; i < lines.size(); i++) {
                LinePlotData line = lines.get(i);

                //Set up calendar for traversal
                Calendar cal = Calendar.getInstance();
                cal.set(1936, Calendar.JANUARY, 1);
                cal.add(Calendar.DAY_OF_YEAR, line.startDay());

                for (int day = 0; day < line.data().size(); day++) {
                    dataset.add(new Day(cal.getTime()),line.data().get(day) * isPercent,line.name());//.add(new Day(cal.getTime()), );
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                }

                renderer.setSeriesPaint(i, line.color());
                renderer.setSeriesStroke(i, new BasicStroke(2.0f));
            }
            plot.setRenderer(renderer);
        }
        else {
            TimeSeriesCollection dataset = new TimeSeriesCollection();
            chart = ChartFactory.createXYLineChart(
                    name,
                    "Day",
                    yAxisName,
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            plot = chart.getXYPlot();

            var renderer = new XYStepRenderer();

            for (int i = 0; i < lines.size(); i++) {
                LinePlotData line = lines.get(i);

                //Set up calendar for traversal
                Calendar cal = Calendar.getInstance();
                cal.set(1936, Calendar.JANUARY, 1);
                cal.add(Calendar.DAY_OF_YEAR, line.startDay());

                var lineData = new TimeSeries(line.name());
                for (int day = 0; day < line.data().size(); day++) {
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    lineData.add(new Day(cal.getTime()), line.data().get(day) * isPercent);
                }

                renderer.setSeriesPaint(i, line.color());
                renderer.setSeriesStroke(i, new BasicStroke(2.0f));
                dataset.addSeries(lineData);
            }
            plot.setRenderer(renderer);
        }
        ValueAxis yAxis = plot.getRangeAxis();
        DateAxis dateAxis = new DateAxis("Date");

        dateAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 7));
        dateAxis.setMinorTickCount(7);

        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);

        //Don't name every weekday
        dateAxis.setTickLabelsVisible(false);

        plot.setDomainAxis(dateAxis);

        switch (type) {
            case Percent -> yAxis.setRange(-100, 100);
            case PositivePercent -> yAxis.setRange(0, 100);
            case PositiveUnboundedPercent, PositiveInteger, PositiveReal -> {
                yAxis.setAutoRange(true);
                yAxis.setLowerBound(0);
            }
            default -> yAxis.setAutoRange(true);//Real, integer, and Unbound percent
        }


        var monthFormat = new SimpleDateFormat("MMM yy");
        //Also print a vertical line every month and year
        var lastDay = dateAxis.getMaximumDate();

        //We will mark the last day of every month
        Calendar cal = Calendar.getInstance();
        cal.set(1935, Calendar.DECEMBER, 31);
        var monthStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.f, new float[]{15.f, 5.f}, 0);
        var yearStroke = new BasicStroke(3.0f);

        //Mark out every month
        while (cal.getTime().before(lastDay)) {
            ValueMarker marker = new ValueMarker(cal.getTime().getTime());
            marker.setLabelBackgroundColor(Color.WHITE);
            if (cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
                marker.setStroke(yearStroke);
            } else {
                marker.setStroke(monthStroke);
            }
            marker.setPaint(Color.BLACK);
            marker.setLabelFont(new Font("Serif", Font.PLAIN, 18));
            marker.setLabelOffset(new RectangleInsets(cal.get(Calendar.MONTH)%2==0?20:40, -30, 0, 0));
            plot.addDomainMarker(marker);
            cal.add(Calendar.MONTH, 1);
            //Display the month we are going into
            marker.setLabel(monthFormat.format(cal.getTime()));
        }
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.black);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 16));
        chart.setTitle(new TextTitle(name, new Font("Serif", java.awt.Font.BOLD, 18)));
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 30, 15));
        chartPanel.setBackground(Color.white);
        add(chartPanel);

        pack();
        this.setSize(defaultWidth, defaultHeight);
        setTitle(name);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Also, while we are at it save to pdf if need be
        if (save) {
            Rectangle pdfPageSize = new Rectangle(defaultWidth, defaultHeight);
            Document document = new Document(pdfPageSize,10,10,10,10);
            try {
                Files.createDirectories(Paths.get("plots"));
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("out/"+name+".pdf"));
                document.open();
                PdfContentByte contentByte = writer.getDirectContent();
                PdfTemplate template = contentByte.createTemplate(defaultWidth, defaultHeight);
                java.awt.Graphics2D g2d = template.createGraphics(defaultWidth, defaultHeight);
                AffineTransform transform = AffineTransform.getScaleInstance(1, 1);
                g2d.transform(transform);

                chart.draw(g2d, new java.awt.geom.Rectangle2D.Double(0, 0, defaultWidth, defaultHeight));

                g2d.dispose();
                contentByte.addTemplate(template, 0, 0);

                document.close();
            } catch (DocumentException | IOException e) {
                document.close();
                throw new RuntimeException("Plot "+name+" failed to save: "+e.getMessage());
            }
        }
    }
}
