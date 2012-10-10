/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.videovlcj.rendered;

import de.dfki.covida.covidacore.utils.ImageUtils;
import de.dfki.covida.videovlcj.IVideoGraphicsHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

/**
 *
 * @author Tobias
 */
public class VideoRenderer extends RenderCallbackAdapter implements IVideoGraphicsHandler {

    /**
     * Logger.
     */
    private Logger log = LoggerFactory.getLogger(VideoRenderer.class);
    /**
     * If true the title of the video is displayed
     */
    private boolean titleOverlayEnabled;
    /**
     * System time in ms when to kill the time code overlay
     */
    private long timeCodeKillTime;
    /**
     * Video frame
     */
    private BufferedImage frame;
    private final int width;
    private final int height;
    private List<Point> drawedPoints;
    private List<Point> shapePoints;
    private Collection<Point> pointsToDraw;
    private BufferedImage preloadFrame;
    private Dimension d;
    private Font f = new Font("Arial", Font.PLAIN, 20);
    private FontMetrics fm;
    private int fh, ascent;
    private int space;
    private String timecode;
    private Color defaultG2DColor = Color.WHITE;
    private String hwr;
    private String title;
    private boolean hwrOverlayEnabled = true;

    /**
     * Constructor
     */
    public VideoRenderer(int width, int height, String title) {
        super(new int[width * height]);
        this.width = width;
        this.height = height;
        this.title = title;
        this.timecode = "";
        this.hwr = "";
        this.drawedPoints = new ArrayList<>();
        this.shapePoints = new ArrayList<>();
        this.pointsToDraw = new ConcurrentLinkedQueue<>();
        this.frame = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height);
        this.frame.setAccelerationPriority(1.0f);
        this.preloadFrame = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height);
        this.preloadFrame.setAccelerationPriority(1.0f);
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
                g2d.drawLine(lastPoint.x + 1, lastPoint.y + 1, point.x + 1, point.y + 1);
                g2d.drawLine(lastPoint.x - 1, lastPoint.y + 1, point.x - 1, point.y + 1);
                g2d.drawLine(lastPoint.x + 1, lastPoint.y - 1, point.x + 1, point.y - 1);
                g2d.drawLine(lastPoint.x - 1, lastPoint.y - 1, point.x - 1, point.y - 1);
                g2d.setColor(defaultG2DColor);
                g2d.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
                lastPoint = point;
            }
        }
    }

    /**
     * Enables / disables time code overlay
     *
     * @param timeout ms how long the overlay is displayed
     */
    public void enableTimeCodeOverlay(long timeout) {
        timeCodeKillTime = System.currentTimeMillis() + timeout;
    }

    /**
     * Enables / disables the tile overlay
     *
     * @param enabled if true the title overlay is enabled
     */
    public void setTitleOverlayEnabled(boolean enabled) {
        titleOverlayEnabled = enabled;
    }

    /**
     * Sets the timecode
     *
     * @param timecode {@link String}
     */
    public synchronized void setTimecode(String timecode) {
        this.timecode = timecode;
    }

    /**
     * Adds a point to {@code pointsToDraw}
     *
     * @param point {@link Point}
     */
    public void draw(Point point) {
        drawedPoints.add(point);
        pointsToDraw.add(point);
    }

    /**
     * Return the video title as {@link String}
     *
     * @return video title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the video image
     *
     * @return {@link BufferedImage}
     */
    public BufferedImage getVideoImage() {
        if (frame == null) {
            log.error("Frame is null.");
        }
        return frame;
    }

    @Override
    public void onDisplay(int[] data) {
        preloadFrame.setRGB(0, 0, width, height, data, 0, width);
        Graphics2D g2d = preloadFrame.createGraphics();
        g2d.setColor(defaultG2DColor);
        BasicStroke bs = new BasicStroke(1);
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
                drawString(timecode, g2d, true, height / 2);
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
                drawString(hwr, g2d, false, height - 70);
                drawString(hwr, g2d, true, height - 70);
            } else {
                log.warn("Can not render hwr result: hwr == null");
                hwrOverlayEnabled = false;
            }
        }
        frame = ImageUtils.deepCopy(preloadFrame);
    }

    @Override
    public synchronized void setShape(List<Point> points) {
        this.shapePoints = points;
        this.pointsToDraw.clear();
        for (Point point : points) {
            pointsToDraw.add(point);
        }
    }

    @Override
    public List<Point> getDrawing() {
        return drawedPoints;
    }

    @Override
    public List<Point> getSavedShape() {
        return shapePoints;
    }

    @Override
    public synchronized void clearShape() {
        shapePoints = new ArrayList<>();
        pointsToDraw.clear();
    }

    @Override
    public synchronized void clearDrawing() {
        drawedPoints = new ArrayList<>();
    }

    @Override
    public void setHWR(String hwr) {
        this.hwr = hwr;
    }
}
