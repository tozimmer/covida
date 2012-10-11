/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidaflvcreator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.ICloseEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoThumbnailsExample {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(VideoThumbnailsExample.class);
    private static Font f = new Font("Arial", Font.PLAIN, 20);
    public static final double SECONDS_BETWEEN_FRAMES = 1;
    private static final String inputFilename = "../covida-res/videos/Collaborative Video Annotation.mp4";
    private static final String outputFilePrefix = "../covida-res/";
    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
    private static int mVideoStreamIndex = -1;
    // Time of last frame write
    private static long mLastPtsWrite = Global.NO_PTS;
    public static final long MICRO_SECONDS_BETWEEN_FRAMES =
            (long) (Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
    private static Color defaultG2DColor = Color.WHITE;
    private static List<Point> drawedPoints;
    private static List<Point> shapePoints;
    private static Collection<Point> pointsToDraw;
    private static Collection<Point> shapeToDraw;

    public static void main(String[] args) {

        IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);

        // stipulate that we want BufferedImages created in BGR 24bit color space
        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

        mediaReader.addListener(new ImageSnapListener());

        // read out the contents of the media file and
        // dispatch events to the attached listener
        while (mediaReader.readPacket() == null) ;

    }

    private static class ImageSnapListener extends MediaListenerAdapter {

        private FontMetrics fm;
        private Dimension d;
        private int ascent;
        private int fh;
        private int space;
        private long timeCodeKillTime;
        private String timecode;
        private int width = 576;
        private int height = 360;
        private boolean titleOverlayEnabled;
        private String title;
        private boolean hwrOverlayEnabled;
        private String hwr;
        private final FLVCreator creator;

        public ImageSnapListener() {
            drawedPoints = new ArrayList<>();
            shapePoints = new ArrayList<>();
            pointsToDraw = new ConcurrentLinkedQueue<>();
            shapeToDraw = new ConcurrentLinkedQueue<>();
            timecode = "FOO";
            timeCodeKillTime = 4000;
            titleOverlayEnabled = true;
            hwrOverlayEnabled = true;
            hwr = "Test";
            title = "Title";
            creator = new FLVCreator("output.flv", width, height);
        }
        
        @Override
        public void onClose(ICloseEvent event) {
            creator.closeStreams();
        }

        @Override
        public void onVideoPicture(IVideoPictureEvent event) {

            if (event.getStreamIndex() != mVideoStreamIndex) {
                // if the selected video stream id is not yet set, go ahead an
                // select this lucky video stream
                if (mVideoStreamIndex == -1) {
                    mVideoStreamIndex = event.getStreamIndex();
                } // no need to show frames from this video stream
                else {
                    return;
                }
            }

            // if uninitialized, back date mLastPtsWrite to get the very first frame
            if (mLastPtsWrite == Global.NO_PTS) {
                mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;
            }

            // if it's time to write the next frame
            if (event.getTimeStamp() - mLastPtsWrite
                    >= MICRO_SECONDS_BETWEEN_FRAMES) {

                dumpImageToFile(event.getImage());

                // indicate file written
                double seconds = ((double) event.getTimeStamp())
                        / Global.DEFAULT_PTS_PER_SECOND;

                // update last write time
                mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
            }

        }

        /**
         * Draws {@link String} on the {@link Graphics2D}
         *
         * @param str {@link String}
         * @param g2d {@link Graphics2D}
         * @param centered {@link Boolean}
         * @param y {@link Integer}
         */
        private void drawString(String str, Graphics2D g2d, boolean centered, int y) {
            StringTokenizer st = new StringTokenizer(str);
            int x = 0;
            String word, sp;
            int wordCount = 0;
            String line = "";
            while (st.hasMoreTokens()) {
                word = st.nextToken();
                if (word.equals("<BR>")) {
                    drawString(g2d, line, fm.stringWidth(line), y + ascent, centered);
                    line = "";
                    wordCount = 0;
                    x = 0;
                    y = y + (fh * 2);
                } else {
                    int w = fm.stringWidth(word);
                    if ((x + space + w) > d.width) {
                        drawString(g2d, line, fm.stringWidth(line), y + ascent, centered);
                        line = "";
                        wordCount = 0;
                        x = 0;
                        y = y + fh;
                    }
                    if (x != 0) {
                        sp = " ";
                    } else {
                        sp = "";
                    }
                    line = line + sp + word;
                    x = x + space + w;
                    wordCount++;
                }
            }
            drawString(g2d, line, fm.stringWidth(line), y + ascent, centered);
        }

        /**
         * Draws {@link String} on the {@link Graphics2D}
         *
         * @param g2d {@link Graphics2D}
         * @param line {@link String}
         * @param lineW {@link Integer}
         * @param y {@link Integer}
         * @param centered {@link Boolean}
         */
        private void drawString(Graphics2D g2d, String line, int lineW, int y, boolean centered) {
            if (centered) {
                g2d.setColor(Color.black);
                g2d.drawString(line, ((d.width - lineW) / 2) - 1, y - 1);
                g2d.drawString(line, ((d.width - lineW) / 2) + 1, y - 1);
                g2d.drawString(line, ((d.width - lineW) / 2) + 1, y + 1);
                g2d.drawString(line, ((d.width - lineW) / 2) - 1, y + 1);
                g2d.setColor(defaultG2DColor);
                g2d.drawString(line, (d.width - lineW) / 2, y);
            } else {
                g2d.setColor(Color.black);
                g2d.drawString(line, 0, y);
                g2d.drawString(line, 0, y);
                g2d.drawString(line, 0, y);
                g2d.drawString(line, 0, y);
                g2d.setColor(defaultG2DColor);
                g2d.drawString(line, 0, y);
            }
        }

        /**
         * Draws {@code pointsToDraw} on {@link Graphics2D}
         *
         * @param g2d {@link Graphics2D}
         */
        private void drawPoints(Graphics2D g2d) {
            Point lastPoint = null;
            for (Point point : pointsToDraw) {
                if (lastPoint == null) {
                    lastPoint = point;
                } else {
                    g2d.setColor(Color.black);
                    g2d.drawLine(lastPoint.x + 2, lastPoint.y + 2, point.x + 2, point.y + 2);
                    g2d.drawLine(lastPoint.x - 2, lastPoint.y + 2, point.x - 2, point.y + 2);
                    g2d.drawLine(lastPoint.x + 2, lastPoint.y - 2, point.x + 2, point.y - 2);
                    g2d.drawLine(lastPoint.x - 2, lastPoint.y - 2, point.x - 2, point.y - 2);
                    g2d.setColor(defaultG2DColor);
                    g2d.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
                    lastPoint = point;
                }
            }
        }

        private void dumpImageToFile(BufferedImage image) {
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(defaultG2DColor);
            BasicStroke bs = new BasicStroke(2);
            g2d.setStroke(bs);
            drawPoints(g2d);
            d = new Dimension(width, height);
            g2d.setFont(f);
            if (fm == null) {
                fm = g2d.getFontMetrics();
                ascent = fm.getAscent();
                fh = ascent + fm.getDescent();
                space = fm.stringWidth(" ");
            }
            if (timeCodeKillTime > System.currentTimeMillis()) {
                if (timecode != null) {
                    drawString(timecode, g2d, true, width / 2);
                } else {
                    log.warn("Can not render time code overlay: timecode == null");
                    timeCodeKillTime = System.currentTimeMillis();
                }
            }
            if (titleOverlayEnabled) {
                if (title != null) {
                    drawString(title, g2d, true, 0);
                } else {
                    log.warn("Can not render title overlay: tile == null");
                    titleOverlayEnabled = false;
                }
            }
            if (hwrOverlayEnabled) {
                if (hwr != null) {
//                drawString(hwr, g2d, false, height - 70);
                    drawString(hwr, g2d, true, width - 70);
                } else {
                    log.warn("Can not render hwr result: hwr == null");
                    hwrOverlayEnabled = false;
                }
            }
            creator.encodeImage(image);
//            try {
//                String outputFilename = outputFilePrefix
//                        + System.currentTimeMillis() + ".png";
//                ImageIO.write(image, "png", new File(outputFilename));
//                return outputFilename;
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
        }
    }
}
