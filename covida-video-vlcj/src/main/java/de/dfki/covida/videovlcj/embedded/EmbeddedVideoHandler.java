/*
 * AbstractVideoHandler.java
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
package de.dfki.covida.videovlcj.embedded;

import de.dfki.covida.videovlcj.AbstractVideoHandler;
import de.dfki.covida.videovlcj.VideoType;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Point;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JWindow;
import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

/**
 * Component to create a {@link MediaPlayer} and {@link VideoRenderer} to play
 * videos.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public final class EmbeddedVideoHandler extends AbstractVideoHandler {

    /**
     * Logger.
     */
    private Logger log = Logger.getLogger(EmbeddedVideoHandler.class);
    /**
     * {@link EmbeddedMediaPlayer}
     */
    private EmbeddedMediaPlayer embeddedMediaPlayer;
    /**
     * Video overlay
     */
    private EmbeddedVideoOverlay overlay;

    /**
     * Creates an instance of {@link AbstractVideoHandler}
     *
     * @param source video source as {@link String}
     * @param height height of the video {@link Quad}
     * @param width width of the video {@link Quad}
     */
    public EmbeddedVideoHandler(String source, String title, Canvas canvas, int height, int width) {
        super(source, title, height, width, VideoType.EMBEDDED);
        CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
        if (mediaPlayer instanceof EmbeddedMediaPlayer) {
            embeddedMediaPlayer = (EmbeddedMediaPlayer) mediaPlayer;
            embeddedMediaPlayer.setVideoSurface(videoSurface);
        }
        overlay = new EmbeddedVideoOverlay(width, height);
        setOverlay(overlay);
    }

    /**
     * Returns the {@link EmbeddedMediaPlayerComponent}
     *
     * @return {@link EmbeddedMediaPlayerComponent}
     */
    public EmbeddedMediaPlayerComponent getMediaPlayerComponent() {
        return mediaPlayerComponent;
    }

    /**
     * Sets video overlay
     * 
     * @param win {@link JWindow}
     */
    public void setOverlay(JWindow win) {
        embeddedMediaPlayer.setOverlay(win);
    }

    /**
     * Enables / Disables the video overlay
     * 
     * @param b {@link Boolean}
     */
    public void enableOverlay(boolean b) {
        embeddedMediaPlayer.enableOverlay(b);
    }

    /**
     * Returns the overlay
     * 
     * @return {@link Window}
     */
    public Window getOverlay() {
        return embeddedMediaPlayer.getOverlay();
    }

    @Override
    public BufferedImage getVideoImage() {
        return getSnapshot();
    }

    @Override
    public BufferedImage getSnapshot() {
        if (mediaPlayerComponent.getMediaPlayer() == null) {
            return null;
        }
        return mediaPlayerComponent.getMediaPlayer().getSnapshot();
    }

    /**
     * Saves the video frame including the shape to {@link File}
     * {@code source + "."+ mediaPlayer.getTime() + ".png"}
     */
    @Override
    public void saveShape() {
        // draw and save new shapes
        if (overlay.getDrawing().size() < 2) {
            log.debug("shapePoints.size()<2");
            overlay.getDrawing().add(new Point(0, 0));
            overlay.getDrawing().add(new Point(0, 0));
        }

        BufferedImage img = getSnapshot();
        if (img != null) {
            try {
                ImageIO.write(img, "png", new File(getSource() + "."
                        + mediaPlayerComponent.getMediaPlayer().getTime() + ".png"));
            } catch (IOException e) {
                log.error(e);
            }
        } else {
            log.warn("Snapshot BufferedImage is null");
        }
        overlay.clearShape();
    }

    /**
     * Adds the {@link Point} to the {@link ShapePoints} which should be draw on
     * the video.
     *
     * @param point {@link Point}
     */
    @Override
    public void draw(Point point) {
        overlay.getDrawing().add(point);
    }

    /**
     * Returns the shape points.
     *
     * @return {@link ShapePoints}
     */
    @Override
    public List<Point> getShape() {
        return overlay.getSavedShape();
    }

    @Override
    public List<Point> getDrawing() {
        return overlay.getDrawing();
    }

    /**
     * Sets the shape points to draw on the video.
     *
     * @param points {@link List}
     */
    @Override
    public void setShape(List<Point> points) {
        overlay.setShape(points);
    }

    @Override
    public void setHWR(String hwr) {
        overlay.setHWR(hwr);
    }

    /**
     * Clears all shapes from the video.
     */
    @Override
    public void clearShape() {
        overlay.clearShape();
    }

    /**
     * Clears all shapes from the video.
     */
    @Override
    public void clearDrawing() {
        overlay.clearDrawing();
    }

    /**
     * Handles positionChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param f time position in percentage as {@link Float}
     */
    @Override
    public void positionChanged(MediaPlayer mp, float f) {
//        if (slider != null) {
//            slider.setSlider(f);
//        }
//        if (controls != null) {
//            controls.highlightPlay();
//        }
        embeddedMediaPlayer.setMarqueeText("VLCJ is quite good");
        embeddedMediaPlayer.setMarqueeSize(60);
        embeddedMediaPlayer.setMarqueeOpacity(70);
        embeddedMediaPlayer.setMarqueeColour(Color.green);
        embeddedMediaPlayer.setMarqueeTimeout(3000);
        embeddedMediaPlayer.setMarqueeLocation(300, 400);
        embeddedMediaPlayer.enableMarquee(true);
        isPlaying = true;
    }

    @Override
    public void setTitleOverlayEnabled(boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void enableTimeCodeOverlay(long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTitle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
