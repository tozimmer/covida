/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidaflvcreator;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author touchandwrite
 */
public class VideoCreatorTest {

    /**
     * Logger
     */
    protected static Logger log = LoggerFactory.getLogger(VideoCreatorTest.class);

    /**
     * Takes a media container (file) as the first argument, opens it and writes
     * some of it's video frames to a *.flv video file
     *
     * @param args {@link String} array
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        List<Point> shape = new ArrayList<>();
        shape.add(new Point(22, 177));
        shape.add(new Point(200, 22));
        shape.add(new Point(155, 43));
        shape.add(new Point(210, 77));
        shape.add(new Point(244, 17));
        shape.add(new Point(22, 7));
        shape.add(new Point(331, 177));
        shape.add(new Point(321, 173));
        shape.add(new Point(100, 177));
        shape.add(new Point(22, 177));

        AnnotatedVideoCreator decodeAndCaptureFrames = new AnnotatedVideoCreator(
                "../covida-res/videos/Collaborative Video Annotation.mp4", 
                5000000, 15000000, shape);
        Thread creatorThread = new Thread(decodeAndCaptureFrames);
        creatorThread.setName("Annotated video creator");
        creatorThread.start();
        while (!decodeAndCaptureFrames.isCompleted()) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException ex) {
                log.error("", ex);
            }
        }
        log.debug("Movie encoded in {}ms: {}", System.currentTimeMillis() - start,
                decodeAndCaptureFrames.getOutfile());
    }
}
