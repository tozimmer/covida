/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.videovlcj.rendered;

import de.dfki.covida.covidacore.data.ShapePoints;
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
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

/**
 *
 * @author Tobias
 */
public class VideoRenderer extends RenderCallbackAdapter implements IVideoGraphicsHandler {

    /**
     * Logger.
     */
    private Logger log = Logger.getLogger(VideoRenderer.class);
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
    private ShapePoints drawedPoints;
    private ShapePoints shapePoints;
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
        this.drawedPoints = new ShapePoints();
        this.shapePoints = new ShapePoints();
        this.frame = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height);
        this.frame.setAccelerationPriority(1.0f);
        this.preloadFrame = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height);
        this.preloadFrame.setAccelerationPriority(1.0f);
    }

    public void enableTimeCodeOverlay(long timeout) {
        timeCodeKillTime = System.currentTimeMillis() + timeout;
    }
    
    public void setTitleOverlayEnabled(boolean enabled){
        titleOverlayEnabled = enabled;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter#onDisplay(
     * int[])
     */
    @Override
    public void onDisplay(int[] data) {
        preloadFrame.setRGB(0, 0, width, height, data, 0, width);
        Graphics2D g2d = preloadFrame.createGraphics();
        g2d.setColor(defaultG2DColor);
        BasicStroke bs = new BasicStroke(1);
        g2d.setStroke(bs);
        drawDrawing(g2d);
        drawShape(g2d);
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
            }else{
                log.warn("Can not render time code overlay: timecode == null");
                timeCodeKillTime = System.currentTimeMillis();
            }
        }
        if (titleOverlayEnabled) {
            if (title != null) {
                drawString(title, g2d, true, 0);
            }else{
                log.warn("Can not render title overlay: tile == null");
                titleOverlayEnabled = false;
            }
        }
        if (hwrOverlayEnabled) {
            if (hwr != null) {
                drawString(hwr, g2d, false, height - 70);
                drawString(hwr, g2d, true, height - 70);
            }else{
                log.warn("Can not render hwr result: hwr == null");
                hwrOverlayEnabled = false;
            }
        }
        frame = ImageUtils.deepCopy(preloadFrame);
    }

    public synchronized void setTimecode(String timecode) {
        this.timecode = timecode;
    }

    @Override
    public synchronized void setShape(ShapePoints points) {
        this.shapePoints = points;
    }

    @Override
    public ShapePoints getDrawing() {
        return drawedPoints;
    }

    @Override
    public ShapePoints getSavedShape() {
        return shapePoints;
    }

    @Override
    public synchronized void clearShape() {
        shapePoints = new ShapePoints();
    }

    @Override
    public synchronized void clearDrawing() {
        drawedPoints = new ShapePoints();
    }
    
    public String getTitle(){
        return title;
    }

    public BufferedImage getVideoImage() {
        if(frame == null){
            log.error("Frame is null.");
        }
        return frame;
    }

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

    private void drawDrawing(Graphics2D g2d) {
        Point lastPoint = null;
        ShapePoints points = drawedPoints.clone();
        for (Point point : points.getShapePoints()) {
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

    private void drawShape(Graphics2D g2d) {
        Point lastPoint = null;
        ShapePoints points = shapePoints.clone();
        for (Point point : points.getShapePoints()) {
            if (lastPoint == null) {
                lastPoint = point;
            } else {
                g2d.setColor(Color.black);
                g2d.drawLine(lastPoint.x + 1, lastPoint.y + 1, point.x + 1, point.y + 1);
                g2d.drawLine(lastPoint.x - 1, lastPoint.y + 1, point.x - 1, point.y + 1);
                g2d.drawLine(lastPoint.x + 1, lastPoint.y - 1, point.x + 1, point.y - 1);
                g2d.drawLine(lastPoint.x - 1, lastPoint.y - 1, point.x - 1, point.y - 1);
                g2d.setColor(Color.YELLOW);
                g2d.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
                lastPoint = point;
            }
        }
    }

    @Override
    public void setHWR(String hwr) {
        this.hwr = hwr;
    }
}
