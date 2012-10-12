/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidaflvcreator;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import de.dfki.covida.covidaflvcreator.utils.ContainerInfo;
import de.dfki.covida.covidaflvcreator.utils.StreamInfo;
import de.dfki.covida.covidaflvcreator.utils.VideoStreamInfo;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Using {@link IMediaReader}, takes a media container, finds the first video
 * stream, decodes that stream, and then writes video frames out to a PNG image
 * file every 5 seconds, based on the video presentation timestamps.
 *
 * @author aclarke
 * @author trebor
 */
public class AnnotatedVideoCreator extends MediaListenerAdapter implements Runnable {

    private static Color defaultG2DColor = Color.WHITE;
    private static Collection<Point> shapeToDraw;
    private FontMetrics fm;
    private int ascent;
    private int fh;
    private int space;
    private boolean titleOverlayEnabled;
    private String title;
    private boolean hwrOverlayEnabled;
    private String hwr;
    private VideoEncoder creator;
    private String timecode;
    private long timeCodeKillTime;
    private final long timeStart;
    private final long timeEnd;
    private static Font f = new Font("Arial", Font.PLAIN, 20);
    /**
     * Logger
     */
    protected Logger log = LoggerFactory.getLogger(getClass());
    /**
     * The video stream index, used to ensure we display frames from one and
     * only one video stream from the media container.
     */
    private int mVideoStreamIndex = -1;
    /**
     * Output file in which the annotated frames shoould be encoded.
     */
    private String outFile;
    /**
     * Indicates if encoding is completed and stream is closed.
     */
    private boolean completed;
    /**
     * Input file from which the frames should be decoded.
     */
    private final String inFile;
    /**
     * Video dimension
     */
    private Dimension dim;

    /**
     * Construct a DecodeAndCaptureFrames which reads and captures frames from a
     * video file.
     *
     * @param inFile the name of the media file to read
     */
    public AnnotatedVideoCreator(String inFile,
            long timeStart, long timeEnd, List<Point> shape) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.inFile = inFile;
        ContainerInfo info = new ContainerInfo(inFile);
        for (StreamInfo si : info.streamInfo) {
            if (si instanceof VideoStreamInfo) {
                VideoStreamInfo vsi = (VideoStreamInfo) si;
                this.dim = new Dimension(vsi.width, vsi.height);
            }
        }
        if (dim != null) {
            shapeToDraw = new ConcurrentLinkedQueue<>();
            for (Point point : shape) {
                shapeToDraw.add(point);
            }
            timecode = "FOO";
            timeCodeKillTime = 4000;
            titleOverlayEnabled = true;
            hwrOverlayEnabled = true;
            hwr = "Test";
            title = "Title";
            outFile = inFile + "_" + timeStart + "_" + timeEnd + ".flv";
            creator = new VideoEncoder(outFile, dim.width,
                    dim.height);
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getOutfile() {
        return outFile;
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
                if ((x + space + w) > dim.width) {
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
            g2d.drawString(line, ((dim.width - lineW) / 2) - 1, y - 1);
            g2d.drawString(line, ((dim.width - lineW) / 2) + 1, y - 1);
            g2d.drawString(line, ((dim.width - lineW) / 2) + 1, y + 1);
            g2d.drawString(line, ((dim.width - lineW) / 2) - 1, y + 1);
            g2d.setColor(defaultG2DColor);
            g2d.drawString(line, (dim.width - lineW) / 2, y);
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
        for (Point point : shapeToDraw) {
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

    /**
     * Called after a video frame has been decoded from a media stream.
     * Optionally a BufferedImage version of the frame may be passed if the
     * calling {@link IMediaReader} instance was configured to create
     * BufferedImages.
     *
     * This method blocks, so return quickly.
     */
    @Override
    public void onVideoPicture(IVideoPictureEvent event) {
        try {
            // if the stream index does not match the selected stream index,
            // then have a closer look
            if (event.getStreamIndex() != mVideoStreamIndex) {
                log.debug("Stream index differs.");
                // if the selected video stream id is not yet set, go ahead an
                // select this lucky video stream
                if (-1 == mVideoStreamIndex) {
                    mVideoStreamIndex = event.getStreamIndex();
                } // otherwise return, no need to show frames from this video stream
                else {
                    log.debug("No need to show frames");
                    return;
                }
            }
            if (event.getTimeStamp() > timeStart && event.getTimeStamp() < timeEnd) {
                BufferedImage image = event.getImage();
                Graphics2D g2d = image.createGraphics();
                g2d.setColor(defaultG2DColor);
                BasicStroke bs = new BasicStroke(2);
                g2d.setStroke(bs);
                drawPoints(g2d);
                g2d.setFont(f);
                if (fm == null) {
                    fm = g2d.getFontMetrics();
                    ascent = fm.getAscent();
                    fh = ascent + fm.getDescent();
                    space = fm.stringWidth(" ");
                }
                if (timeCodeKillTime > System.currentTimeMillis()) {
                    if (timecode != null) {
                        drawString(timecode, g2d, true, dim.width / 2);
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
                        drawString(hwr, g2d, true, dim.width - 70);
                    } else {
                        log.warn("Can not render hwr result: hwr == null");
                        hwrOverlayEnabled = false;
                    }
                }
                creator.encodeImage(image, event.getTimeStamp());
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void run() {
        if (dim != null) {
            IMediaReader reader = ToolFactory.makeReader(inFile);

            // stipulate that we want BufferedImages created in BGR 24bit color space
            reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
            // note that DecodeAndCaptureFrames is derived from
            // MediaReader.ListenerAdapter and thus may be added as a listener
            // to the MediaReader. DecodeAndCaptureFrames implements
            // onVideoPicture().
            boolean addListener = reader.addListener(this);

            if (addListener) {
                // read out the contents of the media file, note that nothing else
                // happens here.  action happens in the onVideoPicture() method
                // which is called when complete video pictures are extracted from
                // the media source

                while (reader.readPacket() == null) {
                    do {
                    } while (false);
                }
            } else {
                log.warn("Attach listener failed.");
            }
            creator.closeStreams();
            completed = true;
        } else {
            outFile = null;
        }
        completed = true;
    }
}
