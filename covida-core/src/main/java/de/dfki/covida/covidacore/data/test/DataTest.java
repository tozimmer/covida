/*
 * DataTest.java
 *
 * Copyright (c) 2012, Tobias Zimmermann All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.dfki.covida.covidacore.data.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationClassList;
import de.dfki.covida.covidacore.data.AnnotationData;
import de.dfki.covida.covidacore.data.DateAdapter;
import de.dfki.covida.covidacore.data.Diagram;
import de.dfki.covida.covidacore.data.Stroke;
import de.dfki.covida.covidacore.data.StrokeList;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data test class.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class DataTest {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(DataTest.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        log.debug("Start Data Test");
        AnnotationData data = AnnotationData.load(new DataTestVideoComponent());
        Annotation annotation = new Annotation();
        annotation.description = "Data Test DFKI";
        annotation.strokelist = new StrokeList();
        StrokeList shapes = new StrokeList();
        Stroke shape = new Stroke();
        shape.points.add(new Point(24, 30));
        shape.points.add(new Point(98, 32));
        shape.points.add(new Point(100, 121));
        shape.points.add(new Point(22, 119));
        shape.points.add(new Point(24, 30));
        shapes.strokelist.add(shape);
        shape = new Stroke();
        shape.points.add(new Point(333, 30));
        shape.points.add(new Point(2, 32));
        shapes.strokelist.add(shape);
        annotation.strokelist = shapes;
        annotation.time_end = (long) 456343;
        annotation.time_start = (long) 455322;
        annotation.creator = "covida";
        annotation.date = Calendar.getInstance().getTime();
        data.save(annotation);
        data.write();
        data = AnnotationData.load(new DataTestVideoComponent());
        data.write();

        AnnotationClassList classes = AnnotationClassList.load();
        classes.write();
        try {
            saveToFile(Diagram.createPieChart(Diagram.createPieDataset()),
                    "test.jpg", 500, 300, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DateAdapter dateAdapter = new DateAdapter();
        // create an empty model
        Model model = ModelFactory.createDefaultModel();

        // create the resource
        //   and add the properties cascading style
        Resource video = model.createResource();
        video.addProperty(DC.title, data.title);
        video.addProperty(DC.source, data.videoSource);
        Resource annot = model.createResource(video);
        annot.addProperty(DC.creator, annotation.creator);
        video.addProperty(RDF.object, annot);
        annot = model.createResource(video);
        annot.addProperty(DC.creator, annotation.creator);
        video.addProperty(RDF.object, annot);
        
        try {
            video.addProperty(DC.date, dateAdapter.marshal(annotation.date));
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(DataTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        // now write the model in XML form to a file
        model.write(System.out);
    }

    public static void saveToFile(JFreeChart chart,
            String aFileName,
            int width,
            int height,
            double quality)
            throws FileNotFoundException, IOException {
        BufferedImage img = draw(chart, width, height);
        ImageIO.write(img, "png", new File(aFileName));
    }

    protected static BufferedImage draw(JFreeChart chart, int width, int height) {
        BufferedImage img =
                new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();

        chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
        g2.dispose();
        return img;
    }
}
