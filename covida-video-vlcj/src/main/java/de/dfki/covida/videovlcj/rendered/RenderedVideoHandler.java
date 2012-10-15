/*
 * RenderedVideoHandler.java
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

import de.dfki.covida.covidacore.components.IVideoComponent;
import de.dfki.covida.covidacore.data.ShapePoints;
import de.dfki.covida.videovlcj.AbstractVideoHandler;
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
     * @param title  video title as {@link String}
     * @param video  corresponding {@link IVideoComponent}
     */
    public RenderedVideoHandler(String source, String title,
            IVideoComponent video) {
        super(source, title, video);
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
    public void saveAnnotatedFrame() {
        if (renderer == null) {
            return;
        }
        BufferedImage img = getSnapshot();
        if (img != null) {
            try {
                ImageIO.write(img, "png", new File(getSource() + "."
                        + mediaPlayer.getTime() + ".png"));
            } catch (IOException e) {
                log.error("", e);
            }
        } else {
            log.warn("Snapshot BufferedImage is null");
        }
        renderer.clearShapes();
    }

    @Override
    public void draw(Point point) {
        if (renderer == null) {
            return;
        }
        renderer.draw(point);
    }

    @Override
    public List<ShapePoints> getShapes() {
        if (renderer == null) {
            return null;
        }
        return renderer.getSavedShapes();
    }

    @Override
    public List<ShapePoints> getDrawings() {
        if (renderer == null) {
            return null;
        }
        return renderer.getDrawings();
    }

    @Override
    public void addShape(List<Point> points) {
        if (renderer == null) {
            return;
        }
        renderer.addShape(points);
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
        renderer.clearShapes();
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

    @Override
    public void endDrawStroke() {
        renderer.endDrawStroke();
    }
}
