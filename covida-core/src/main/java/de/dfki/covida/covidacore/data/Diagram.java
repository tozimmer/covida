/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data;

import de.dfki.covida.covidacore.utils.ImageUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

/**
 *
 * @author Tobias
 */
public class Diagram {

    private static JFreeChart chart;
    private static JFreeChart chart2;
    private static JFreeChart chart3;
    private static Image transpImg;
    private static Image transpImg2;
    private static Image transpImg3;

    // BUILD THE PIE CHART
    public static JFreeChart createPieChart(PieDataset dataset) {

        JFreeChart chart = ChartFactory.createRingChart(
                "Chart Title", // chart title
                dataset, // data
                true, // include legend
                true,
                false);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setSectionOutlinesVisible(false);
        plot.setNoDataMessage("No data available");
        PieDataset[] datasets = new PieDataset[3];
        datasets[0] = dataset;
        datasets[1] = dataset;
        datasets[2] = dataset;
        createConcentricRingChart(datasets, new Dimension(600, 600), 
                Boolean.TRUE);
        return chart;

    }

    // CREATE THE PIE CHART DATA
    public static PieDataset createPieDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        dataset.setValue("One", new Double(43.2));
        dataset.setValue("Two", new Double(10.0));
        dataset.setValue("Three", new Double(27.5));
        dataset.setValue("Four", new Double(17.5));
        dataset.setValue("Five", new Double(11.0));
        dataset.setValue("Six", new Double(19.4));


        return dataset;
    }

    public static void createConcentricRingChart(PieDataset[] dataset, Dimension size, Boolean legend) {
        try {
            int i = size.height;
        } catch (NullPointerException e) {  //no size passed to constructor
            size = new Dimension(400, 400);
        }

        //create charts
        createCharts(dataset, legend);

        //build get buffered image of first chart and get its background color
        BufferedImage bufImg = chart.createBufferedImage(size.width, size.height);
        int backgroundRGB = bufImg.getRGB(1, 1);

        //get a buffered image of each chart converting the background color to transparent
        transpImg = TransformColorToTransparency(bufImg,
                new Color(backgroundRGB),
                new Color(backgroundRGB));
        BufferedImage image = ImageUtils.toBufferedImage(transpImg);
        try {
            ImageIO.write(image, "png", new File("test.png"));
        } catch (IOException ex) {
            Logger.getLogger(Diagram.class.getName()).log(Level.SEVERE, null, ex);
        }
        transpImg2 = TransformColorToTransparency(chart2.createBufferedImage(size.width, size.height),
                new Color(backgroundRGB),
                new Color(backgroundRGB));
        transpImg3 = TransformColorToTransparency(chart3.createBufferedImage(size.width, size.height),
                new Color(backgroundRGB),
                new Color(backgroundRGB));
    }

    private static void createCharts(PieDataset dataset[], Boolean legend) {
        RingPlot plot, plot2, plot3;
        chart = ChartFactory.createRingChart(
                null, //title
                dataset[0],
                legend, //legend 
                false, //tooltips
                false); //urls
        //leave background paint for first chart for border
        //chart.setBackgroundPaint(null);
        plot = (RingPlot) chart.getPlot();
        plot.setCircular(false);
        plot.setLabelGenerator(null);//don't paint labels
        plot.setInnerSeparatorExtension(0);
        plot.setOuterSeparatorExtension(0);
        plot.setSectionDepth(0.5);
        plot.setInteriorGap(0.27);

        chart2 = ChartFactory.createRingChart( //test for concentric
                null, //title
                dataset[1],
                //TODO layout dataset[1] & [2] legends if different from [0]
                legend, //legend
                false, //tooltips
                false); //urls
        chart2.setBackgroundPaint(null);//transparent

        plot2 = (RingPlot) chart2.getPlot();
        plot2.setBackgroundPaint(null);//transparent
        plot2.setLabelGenerator(null);//don't paint labels
        plot2.setCircular(false);
        plot2.setInnerSeparatorExtension(0);
        plot2.setOuterSeparatorExtension(0);
        plot2.setSectionDepth(0.3);
        plot2.setInteriorGap(0.13);

        chart3 = ChartFactory.createRingChart( //test for concentric
                null, //title
                dataset[2],
                legend, //legend
                false, //tooltips
                false); //urls
        chart3.setBackgroundPaint(null);//transparent

        plot3 = (RingPlot) chart3.getPlot();
        plot3.setBackgroundPaint(null);//transparent?
        //plot2.setBackgroundAlpha(0.0f);
        //plot2.setForegroundAlpha(0.5f);
        plot3.setLabelGenerator(null);//don't paint labels
        plot3.setSectionDepth(0.2);
        plot3.setCircular(false);
        plot3.setInnerSeparatorExtension(0);
        plot3.setOuterSeparatorExtension(0);
        plot3.setInteriorGap(0.00);
    }

    // if pixel color is between c1 and c2 (inclusive), make it transparent
    private static Image TransformColorToTransparency(BufferedImage image, Color c1, Color c2) {
        // Primitive test, just an example
        final int r1 = c1.getRed();
        final int g1 = c1.getGreen();
        final int b1 = c1.getBlue();
        final int r2 = c2.getRed();
        final int g2 = c2.getGreen();
        final int b2 = c2.getBlue();
        ImageFilter filter = new RGBImageFilter() {
            @Override
            public final int filterRGB(int x, int y, int rgb) {
                int r = (rgb & 0x00ff0000) >> 16;
                int g = (rgb & 0x0000ff00) >> 8;
                int b = rgb & 0x000000ff;
                if (r >= r1 && r <= r2
                        && g >= g1 && g <= g2
                        && b >= b1 && b <= b2) {
                    // Set fully transparent but keep color
                    return rgb & 0x00FFFFFF; //alpha of 0 = transparent
                }
                return rgb;
            }
        };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }
}
