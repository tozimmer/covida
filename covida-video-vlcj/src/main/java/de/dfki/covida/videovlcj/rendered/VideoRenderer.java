/*
 * VideoRenderer.java
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
package de.dfki.covida.videovlcj.rendered;

import de.dfki.covida.covidacore.data.Stroke;
import de.dfki.covida.covidacore.data.StrokeList;
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
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

/**
 * Video Renderer
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
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
    private List<Stroke> drawedPoints;
    private StrokeList shapePoints;
    private Collection<Collection<Point>> pointsToDraw;
    private Collection<Polygon> shapeToDraw;
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
        this.shapePoints = new StrokeList();
        this.pointsToDraw = new ConcurrentLinkedQueue<>();
        shapeToDraw = new ConcurrentLinkedQueue<>();
        this.frame = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height);
        this.frame.setAccelerationPriority(1.0f);
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
        for (Collection<Point> points : pointsToDraw) {
            Point lastPoint = null;
            for (Point point : points) {
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
        for (Polygon polygon : shapeToDraw) {
            g2d.setColor(Color.black);
            polygon.translate(+2, +2);
            g2d.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
            polygon.translate(-4, +0);
            g2d.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
            polygon.translate(+4, -4);
            g2d.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
            polygon.translate(-4, +0);
            g2d.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
            g2d.setColor(Color.yellow);
            polygon.translate(+2, +2);
            g2d.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
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
    @Override
    public synchronized void setTimecode(String timecode) {
        this.timecode = timecode;
    }

    /**
     * Adds a point to {@code pointsToDraw}
     *
     * @param point {@link Point}
     */
    public void draw(Point point) {
        if (drawedPoints.isEmpty()) {
            Stroke stroke = new Stroke();
            drawedPoints.add(stroke);
        }
        drawedPoints.get(drawedPoints.size() - 1).points.add(point);
        Iterator<Collection<Point>> iterator = pointsToDraw.iterator();
        Collection<Point> last = null;
        while (iterator.hasNext()) {
            last = iterator.next();
        }
        if (last == null) {
            last = new ConcurrentLinkedQueue<>();
        }
        last.add(point);
        pointsToDraw.add(last);
    }

    public void endDrawStroke() {
        drawedPoints.add(new Stroke());
        Collection<Point> newStroke = new ConcurrentLinkedQueue<>();
        pointsToDraw.add(newStroke);
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
            return null;
        }
        BufferedImage image = ImageUtils.deepCopy(frame);
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
                drawString(hwr, g2d, true, height - 70);
            } else {
                log.warn("Can not render hwr result: hwr == null");
                hwrOverlayEnabled = false;
            }
        }
        return image;
    }

    @Override
    public void onDisplay(int[] data) {
        frame.setRGB(0, 0, width, height, data, 0, width);
        data = null;
    }

    @Override
    public synchronized void addShape(Stroke stroke) {
        Polygon polygon = new Polygon();
        for (Point point : stroke.points) {
            polygon.addPoint(point.x, point.y);
        }
        this.shapePoints.strokelist.add(stroke);
        this.pointsToDraw.clear();
        shapeToDraw.add(polygon);
    }

    @Override
    public List<Stroke> getDrawings() {
        return drawedPoints;
    }

    @Override
    public StrokeList getSavedShapes() {
        return shapePoints;
    }

    @Override
    public synchronized void clearShapes() {
        shapePoints = new StrokeList();
        shapeToDraw.clear();
    }

    @Override
    public synchronized void clearDrawing() {
        drawedPoints = new ArrayList<>();
        pointsToDraw.clear();
    }

    @Override
    public void setHWR(String hwr) {
        this.hwr = hwr;
    }

    @Override
    public void clear() {
        frame = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height);
    }
}
