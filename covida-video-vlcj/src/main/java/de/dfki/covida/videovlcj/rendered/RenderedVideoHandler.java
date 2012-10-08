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
package de.dfki.covida.videovlcj.rendered;

import de.dfki.covida.videovlcj.AbstractVideoHandler;
import de.dfki.covida.videovlcj.VideoType;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.player.MediaPlayer;

/**
 * Component to create a {@link MediaPlayer} and {@link VideoRenderer} to play
 * videos.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class RenderedVideoHandler extends AbstractVideoHandler {

    /**
     * Logger.
     */
    private Logger log = LoggerFactory.getLogger(RenderedVideoHandler.class);

    /**
     * Creates an instance of {@link AbstractVideoHandler}
     *
     * @param source video source as {@link String}
     * @param height height of the video {@link Quad}
     * @param width width of the video {@link Quad}
     */
    public RenderedVideoHandler(String source, String title, int height, int width) {
        super(source, title, height, width, VideoType.RENDERED);
    }

    @Override
    public void enableTimeCodeOverlay(long timeout) {
        renderer.enableTimeCodeOverlay(timeout);
    }

    @Override
    public void setTitleOverlayEnabled(boolean enabled) {
        renderer.setTitleOverlayEnabled(enabled);
    }

    @Override
    public BufferedImage getVideoImage() {
        return renderer.getVideoImage();
    }

    /**
     * Makes a snapshot of the video
     *
     * @return video snapshot as {@link BufferedImage}
     */
    @Override
    public BufferedImage getSnapshot() {
        if (renderer == null) {
            return null;
        }
        return renderer.getVideoImage();
    }

    /**
     * Saves the video frame including the shape to {@link File}
     * {@code source + "."+ mediaPlayer.getTime() + ".png"}
     */
    @Override
    public void saveShape() {
        if (renderer == null) {
            return;
        }
        // draw and save new shapes
        if (renderer.getDrawing().size() < 2) {
            log.debug("shapePoints.size()<2");
            renderer.getDrawing().add(new Point(0, 0));
            renderer.getDrawing().add(new Point(0, 0));
        }

        BufferedImage img = getSnapshot();
        if (img != null) {
            try {
                ImageIO.write(img, "png", new File(getSource() + "."
                        + mediaPlayer.getTime() + ".png"));
            } catch (IOException e) {
                log.error("",e);
            }
        } else {
            log.warn("Snapshot BufferedImage is null");
        }
        renderer.clearShape();
    }

    /**
     * Adds the {@link Point} to the {@link ShapePoints} which should be draw on
     * the video.
     *
     * @param point {@link Point}
     */
    @Override
    public void draw(Point point) {
        if (renderer == null) {
            return;
        }
        renderer.draw(point);
    }

    /**
     * Returns the shape points.
     *
     * @return {@link ShapePoints}
     */
    @Override
    public List<Point> getShape() {
        if (renderer == null) {
            return null;
        }
        return renderer.getSavedShape();
    }

    @Override
    public List<Point> getDrawing() {
        if (renderer == null) {
            return null;
        }
        return renderer.getDrawing();
    }

    /**
     * Sets the shape points to draw on the video.
     *
     * @param points {@link ShapePoints}
     */
    @Override
    public void setShape(List<Point> points) {
        if (renderer == null) {
            return;
        }
        renderer.setShape(points);
    }

    @Override
    public void setHWR(String hwr) {
        if (renderer == null) {
            log.warn("Cannot set HWR result: HWR result is == null");
            return;
        }
        renderer.setHWR(hwr);
    }

    /**
     * Clears all shapes from the video.
     */
    @Override
    public void clearShape() {
        if (renderer == null) {
            return;
        }
        renderer.clearShape();
    }

    /**
     * Clears all shapes from the video.
     */
    @Override
    public void clearDrawing() {
        if (renderer == null) {
            return;
        }
        renderer.clearDrawing();
    }

    @Override
    public String getTitle() {
        return renderer.getTitle();
    }
}
