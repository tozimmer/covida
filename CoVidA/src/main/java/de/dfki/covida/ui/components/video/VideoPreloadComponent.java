/*
 * VideoPreloadComponent.java
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
package de.dfki.covida.ui.components.video;

import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jmex.awt.swingui.ImageGraphics;
import de.dfki.covida.data.VideoFormat;
import de.dfki.covida.ui.components.CovidaComponent;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;

/**
 * Component to preload videos for dimension detection
 *
 * @author Tobias Zimmermann
 *
 */
public class VideoPreloadComponent extends CovidaComponent implements Runnable {

    /**
     * Generated serial id.
     */
    private static final long serialVersionUID = 3695261146791636755L;
    protected final AlphaComposite TRANSPARENT = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.0f);
    protected final AlphaComposite SOLID = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 1.0f);
    private File videoFile;
    private String videoSource;
    private MediaPlayerFactory factory;
    private DirectMediaPlayer mediaPlayer;
    private String mrl;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(VideoPreloadComponent.class);
    private int height;
    private VideoFormat format;
    private TextureState ts;
    private Texture2D texture;
    private ImageGraphics g2d;
    private Spatial videoQuad;


    @Override
    protected void touchBirthAction(TouchActionEvent e) {
       
    }

    @Override
    protected void touchAliveAction(TouchActionEvent e) {
       
    }

    @Override
    protected void touchDeadAction(TouchActionEvent e) {
        
    }

    /**
     * Callback for rendering the video.
     *
     * @author Tobias Zimmermann
     *
     */
    class RenderCallback extends RenderCallbackAdapter {

        private BufferedImage image;

        /**
         * Constructor
         */
        public RenderCallback() {
            super(new int[getWidth() * getHeight()]);
            image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());
            image.setAccelerationPriority(1.0f);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter#onDisplay(
         * int[])
         */
        public void onDisplay(int[] data) {
            image.setRGB(0, 0, getWidth(), getHeight(), data, 0, getWidth());
            updateVideo(image);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jme.scene.TriMesh#draw(com.jme.renderer.Renderer)
     */
    @Override
    public void draw(Renderer r) {
        try {
            if (g2d != null && texture.getTextureId() > 0) {
                g2d.update(texture, false);
            }
        } catch (Exception e) {
            log.error(e);
        }
        super.draw(r);
    }

    /**
     * Updates the video image.
     *
     * @param image
     */
    public void updateVideo(BufferedImage image) {
        g2d.drawImage(image, null, 0, 0);
        g2d.update();
    }

    /**
     * VideoComponent Constructor
     *
     * @param id
     * @param x
     * @param y
     * @param source
     * @param format
     * @param HEIGHT
     * @param repeating
     * @param node
     */
    public VideoPreloadComponent(String source) {
        super();
        videoSource = source;
        log.debug("VIDEO SOURCE (PRELOAD): " + this.videoSource);
        height = 1;
        format = new VideoFormat(1);
        videoQuad = new Quad("video", 10, 10);
        videoQuad.setLocalTranslation(display.x, display.y, 0);
        this.attachChild(videoQuad);
    }

    /**
     * Initialize the VideoComponent
     *
     */
    @Override
    public void initComponent() {
        super.initComponent();
        generateTexture();
        factory = new MediaPlayerFactory();
        mediaPlayer = factory.newDirectMediaPlayer(getWidth(), getHeight(), new RenderCallback());
    }

    /**
     * Creates a texture which can be used to draw the pen information.
     */
    private void generateTexture() {
        // ---- Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = new Texture2D();
        texture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        texture.setMinificationFilter(Texture.MinificationFilter.BilinearNoMipMaps);
        texture.setWrap(Texture.WrapMode.Repeat);
        // ---- Drawable image initialization ----
        g2d = ImageGraphics.createInstance(getWidth(), getHeight(), 0);
        BlendState alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
        alpha.setEnabled(true);
        alpha.setBlendEnabled(true);

        alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alpha.setTestEnabled(true);
        alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
        texture.setImage(g2d.getImage());
        ts.setTexture(texture);
        // this.videoQuad.setRenderState(alpha);
        videoQuad.setRenderState(ts);
        videoQuad.updateRenderState();
    }

    /**
     *
     * @return the videoSource String
     */
    public String getFile() {
        return this.videoSource;
    }

    /**
     * Makes a snapshot of the video
     *
     * @return video snapshot
     */
    public BufferedImage getSnapshot() {
        return this.mediaPlayer.getSnapshot();
    }

    /**
     *
     * @return true if video is ready to play
     */
    public boolean isReady() {
        if (mediaPlayer != null) {
            return true;
        }
        return false;
    }

    /**
     * Start video (Initialisation)
     */
    public void start() {
        if (this.videoFile != null) {
            mediaPlayer.playMedia(this.videoFile.toString());
            this.mrl = this.videoFile.toString();
        } else {
            mediaPlayer.playMedia(videoSource);
            this.mrl = videoSource;
        }
        mediaPlayer.setPlaySubItems(true);
    }

    /**
     * Stops the video
     */
    public void stop() {
        if (isReady()) {
            this.mediaPlayer.prepareMedia(mrl);
            this.mediaPlayer.stop();
        }
    }

    /**
     * Plays the actual media from the beginning
     */
    public void replay() {
        if (isReady()) {
            this.mediaPlayer.playMedia(mrl);
        }
    }

    /**
     * Changes the actual playing media
     *
     * @param mrl
     */
    public void setMedia(String mrl) {
        this.mrl = mrl;
        if (isReady()) {
            this.mediaPlayer.playMedia(mrl);
        }
    }

    /**
     * Pauses the video
     */
    public void pause() {
        if (isReady()) {
            if (this.mediaPlayer.isPlaying()) {
                this.mediaPlayer.pause();
            }
        }
    }

    /**
     * Resumes a paused video
     *
     */
    public void resume() {
        if (isReady()) {
            if (!this.mediaPlayer.isPlaying()) {
                this.mediaPlayer.play();
            }
        }
    }

    /**
     * Sets the video time position in frames
     *
     * @param time - time in frames
     */
    public void setTimePosition(long time) {
        if (isReady()) {
            mediaPlayer.setTime(time);
        }
    }

    /**
     * Sets the video time position in percentage
     *
     * @param percentage
     */
    public void setTimePosition(float percentage) {
        if (isReady()) {
            mediaPlayer.setTime((long) (percentage * getMaxTime()));
        }
    }

    /**
     * Returns video time position (Frame)
     *
     * @return
     * <code>long</code> lenght , -1 if no video is active
     */
    public long getFramePosition() {
        if (isReady()) {
            return mediaPlayer.getTime();
        }
        return -1;
    }

    /**
     * Returns video time position (ms)
     *
     * @return
     * <code>long</code> lenght , -1 if no video is active
     */
    public float getTimePosition() {
        if (isReady()) {
            return (mediaPlayer.getTime())
                    / mediaPlayer.getFps();
        }
        return -1;
    }

    public String getVideoProgress() {
        int p = (int) (mediaPlayer.getPosition() * 100);
        return String.valueOf(p) + " %";
    }

    /**
     * Returns video source length in frames
     *
     * @return
     * <code>long</code> lenght , -1 if no video is active
     * @return -1 if video is not ready;
     */
    public long getMaxTime() {
        if (isReady()) {
            return mediaPlayer.getLength();
        }
        return -1;
    }

    /**
     *
     * @return fps - frames per second
     * @return -1 if video is not Ready
     */
    public float getFps() {
        if (isReady()) {
            return mediaPlayer.getFps();
        }
        return -1;
    }

    public void setSpeed(float rate) {
        if (isReady()) {
            mediaPlayer.setRate(rate);
        }
    }

    public float getSpeed() {
        if (isReady()) {
            return mediaPlayer.getRate();
        }
        return -1;
    }

    public void resetSpeed() {
        if (isReady()) {
            mediaPlayer.setRate(1.f);
        }
    }

    /**
     * Changes video source of VideoComponent
     *
     * @param file
     */
    public void videoChange(File file) {
        this.videoFile = file;
        this.mrl = file.toString();
    }

    /**
     * Releases native sources
     */
    @Override
    public boolean cleanUp() {
        log.debug("cleanup video");
        mediaPlayer.release();
        factory.release();
        log.debug("cleanup complete");
        return true;
    }

    /**
     * Sets the volume for the video.
     *
     * @param value -
     * <code>Integer</code> - Volume in percentage
     */
    public void setVolume(int value) {
        if (isReady()) {
            this.mediaPlayer.setVolume(value);
        }
    }

    public Dimension getVideoDimension() {
        if (isReady()) {
            return mediaPlayer.getVideoDimension();
        }
        return null;
    }

    @Override
    public Node getRootNode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void dragAction(DragEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void rotationAction(RotationGestureEvent event) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void zoomAction(ZoomEvent event) {
        // TODO Auto-generated method stub
    }

    /**
     * Method for calculation of video width
     *
     * @return video width
     */
    public int getWidth() {
        return format.determineWidth(getHeight());
    }

    /**
     *
     * @return height of the VideoComponent
     */
    public int getHeight() {
        return height;
    }

    @Override
    public void run() {
        initComponent();
        start();
        setVolume(0);
    }

    @Override
    protected void touchDeadAction(int touchId) {
        // TODO Auto-generated method stub
    }
}
