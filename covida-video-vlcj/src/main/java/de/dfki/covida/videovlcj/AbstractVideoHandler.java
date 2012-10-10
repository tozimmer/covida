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
package de.dfki.covida.videovlcj;

import com.sun.jna.Platform;
import de.dfki.covida.covidacore.utils.VideoUtils;
import de.dfki.covida.videovlcj.rendered.RenderedVideoHandler;
import de.dfki.covida.videovlcj.rendered.VideoRenderer;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurface;

/**
 * Component to create a {@link MediaPlayer} and {@link VideoRenderer} to play
 * videos.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public abstract class AbstractVideoHandler implements MediaPlayerEventListener {

    /**
     * Logger.
     */
    private Logger log = LoggerFactory.getLogger(AbstractVideoHandler.class);
    /**
     * height of the video video
     */
    private final int height;
    /**
     * width of the video video
     */
    private final int width;
    /**
     * video source as {@link String}
     */
    private final String source;
    /**
     * If true the video is playing.
     */
    protected boolean isPlaying;
    /**
     * If true the {@code timeStart} and {@code timeEnd} is considered.
     *
     * @see #setTimeRange(long, long)
     */
    protected boolean isTimeRanged;
    /**
     * Defines the start time of the video. Note that {@code timeStart} is only
     * considered if {@code isTimeRange} is true.
     */
    protected long timeStart;
    /**
     * Defines the end time of the video. Note that {@code timeEnd} is only
     * considered if {@code isTimeRange} is true.
     */
    protected long timeEnd;
    /**
     * Video Slider {@link ISlider}
     */
    protected ISlider slider;
    /**
     * Video controls {@link IVideoControls}
     */
    protected IVideoControls controls;
    /**
     * If tue the video repeating is enabled.
     */
    protected Boolean repeat;
    /**
     * Current recognized handwriting Note that this {@link String} must be set
     * by the visual component of covida
     */
    protected String hwr;
    /**
     * {@link MediaPlayer} instance to play the {@code source}
     */
    protected MediaPlayer mediaPlayer;
    /**
     * Embedded media player, used by {@link EmbeddedMediaPlayerComponent}
     */
    protected EmbeddedMediaPlayerComponent mediaPlayerComponent;
    /**
     * {@link VideoRenderer} Note that this variable is only used by
     * {@link RenderedVideoHandler}
     */
    protected VideoRenderer renderer;
    /**
     * The {@link MediaPlayerFactory} to crate {@link VideoSurface} or
     * {@link MediaPlayer}
     */
    protected final MediaPlayerFactory mediaPlayerFactory;

    /**
     * Creates an instance of {@link AbstractVideoHandler}
     *
     * @param source video source as {@link String}
     * @param height height of the video {@link Quad}
     * @param width width of the video {@link Quad}
     */
    public AbstractVideoHandler(String source, String title, int height, int width, VideoType videoType) {
        String[] args;
        if (Platform.isMac()) {
            args = new String[]{"--no-video-title-show", "--vout=macosx"};
        } else {
            args = new String[]{"--no-video-title-show"};
        }
        this.mediaPlayerFactory = new MediaPlayerFactory(args);
        if (videoType == VideoType.EMBEDDED) {
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            mediaPlayer = mediaPlayerComponent.getMediaPlayer();
        } else if (videoType == VideoType.RENDERED) {
            this.renderer = new VideoRenderer(width, height, title);
            mediaPlayer = mediaPlayerFactory.newDirectMediaPlayer(width, height, renderer);
        }
        this.source = source;
        this.height = height;
        this.width = width;
        addEventListener();
    }

    /**
     * Adds the {@link AbstractVideoHandler} as Listener to {@link MediaPlayer}
     */
    private void addEventListener() {
        if (mediaPlayer != null) {
            mediaPlayer.addMediaPlayerEventListener(this);
        } else {
            log.warn("Could not set EventListener, mediaPlayer == null!");
        }
    }

    /**
     * Returns the {@link Dimension} of the video.
     *
     * @return {@link Dimension} of the video.
     * @return {@link null} if video is not created or activated.
     */
    public Dimension getDimension() {
        return new Dimension(getWidth(), getHeight());
    }

    /**
     * Returns the current time postion in ms.
     *
     * Note that only the part from {@code timeStart) to {
     *
     * @timeEnd} is considered.
     * @see #setTimeRange(long, long)
     *
     * @return
     */
    public long getTime() {
        if (mediaPlayer == null) {
            return -1;
        }
        if (isTimeRanged) {
            if (isReady()) {
                return mediaPlayer.getTime() - timeStart;
            }
            return -1;
        } else {
            if (isReady()) {
                return mediaPlayer.getTime();
            }
            return -1;
        }
    }

    /**
     * Returns the video progess in percent as {@link String}.
     *
     * Note that only the part from {@code timeStart) to {
     *
     * @timeEnd} is considered.
     * @see #setTimeRange(long, long)
     *
     * @return vieo progress in the format xx % (xx is the progress in percent)
     */
    public String getVideoProgress() {
        if (mediaPlayer == null) {
            return null;
        }
        if (!isTimeRanged) {
            int p = (int) (mediaPlayer.getPosition() * 100);
            return String.valueOf(p) + " %";
        } else {
            int p = (int) ((getTime() / getMaxTime()) * 100);
            return String.valueOf(p) + " %";
        }
    }

    /**
     * Start video
     */
    public void start() {
        if (mediaPlayer == null) {
            log.warn("Could not start video, mediaPlayer == null!");
            return;
        }
        mediaPlayer.playMedia(getSource());
        mediaPlayer.setPlaySubItems(true);
    }

    /**
     * Returns the status of the video.
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
     * Returns the shape points.
     *
     * @return {@link ShapePoints}
     */
    abstract public List<Point> getShape();

    abstract public List<Point> getDrawing();

    /**
     * Sets the shape points to draw on the video.
     *
     * @param points {@link List}
     */
    abstract public void setShape(List<Point> points);

    /**
     * Sets the video slider for the video {@link ISlider}
     *
     * @param slider Video slider {@link ISlider}
     */
    public void setSlider(ISlider slider) {
        this.slider = slider;
    }

    /**
     * Sets the video control elements {@link IVideoControls}
     *
     * @param controls Video controls {@link IVideoControls}
     */
    public void setControls(IVideoControls controls) {
        this.controls = controls;
    }

    /**
     * Returns the {@link AbstractVideoHandler} status.
     *
     * @return true if video is active
     */
    public boolean isActive() {
        if (mediaPlayer == null) {
            return false;
        }
        return (mediaPlayer.isPlaying());
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getVolume() {
        return mediaPlayer.getVolume();
    }

    /**
     * Clean up all resources of the {@link AbstractVideoHandler}
     */
    public void cleanUp() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (mediaPlayerComponent != null) {
            mediaPlayerComponent.release();
        }
//        if (mediaPlayerFactory != null) {
//            mediaPlayerFactory.release();
//        }
    }

    /**
     * Stops the video.
     */
    public void stop() {
        isPlaying = false;
        if (mediaPlayer == null) {
            return;
        }
        if (isReady()) {
            setTimePostion(0);
            mediaPlayer.stop();
            isPlaying = false;
            controls.highlightStop();
            controls.highlightPlay();
        }
    }

    /**
     * Sets the media source of the video.
     *
     * @param source video source as {@link String}
     */
    public void setMedia(String source) {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.prepareMedia(source);
        mediaPlayer.playMedia(source);
    }

    /**
     * Pauses the video.
     */
    public void pause() {
        if (mediaPlayer == null) {
            return;
        }
        if (isReady()) {
            if (this.mediaPlayer.isPlaying()) {
                this.mediaPlayer.pause();
                isPlaying = false;
            }
        }
        isPlaying = false;
    }

    /**
     * Resumes the video.
     */
    public void resume() {
        if (mediaPlayer == null) {
            return;
        }
        if (isReady()) {
            if (!isPlaying) {
                this.mediaPlayer.play();
                isPlaying = true;
            }
        }
    }

    /**
     * Sets the time position of the video in percentage.
     *
     * Note that only the part from {@code timeStart) to {
     *
     * @timeEnd} is considered if {@code isTimeRanged} is {@code true}.
     * @see #setTimeRange(long, long)
     *
     * @param time
     */
    public void setTimePosition(long time) {
        if (mediaPlayer == null) {
            return;
        }
        if (isTimeRanged) {
            if (isReady()) {
                mediaPlayer.setTime(time + timeStart);
            }
        } else {
            if (isReady()) {
                mediaPlayer.setTime(time);
            }
        }
    }

    /**
     * Returns the video source as {@link String}.
     *
     * @return video source as {@link String}
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the video width as {@link Integer}.
     *
     * @return video width as {@link Integer}
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the video height as {@link Integer}.
     *
     * @return video height as {@link Integer}
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the time position of the video in percentage.
     *
     * Note that only the part from {@code timeStart) to {
     *
     * @timeEnd} is considered.
     * @see #setTimeRange(long, long)
     *
     * @param percentage
     */
    public void setTimePostion(float percentage) {
        if (mediaPlayer == null) {
            return;
        }
        if (isTimeRanged) {
            if (isReady()) {
                mediaPlayer.setTime((long) ((percentage * (getMaxTime() - timeStart)) + timeStart));
            }
        } else {
            if (isReady()) {
                mediaPlayer.setTime((long) (percentage * getMaxTime()));
            }
        }
    }

    /**
     * Returns the max time position of the video in ms.
     *
     * Note that only the part from {@code timeStart) to {@code timeEnd} is considered.
     *
     * @see #setTimeRange(long, long)
     *
     * @return max time position
     */
    public long getMaxTime() {
        if (mediaPlayer == null) {
            return -1;
        }
        if (isTimeRanged) {
            if (isReady()) {
                return timeEnd;
            }
            return -1;
        } else {
            if (isReady()) {
                return mediaPlayer.getLength();
            }
            return -1;
        }
    }

    /**
     * Sets the volume of the video.
     *
     * @param volume percentage of video volume as {@link Integer}
     */
    public void setVolume(int volume) {
        if (isReady()) {
            this.mediaPlayer.setVolume(volume);
        }
    }

    /**
     * Sets a time range for the video. Only the part from start to long (time
     * in ms) is played.
     *
     * @param start start time in ms
     * @param end end time in ms
     */
    public void setTimeRange(long start, long end) {
        this.isTimeRanged = true;
        this.timeStart = start;
        this.timeEnd = end;
    }

    /**
     * Clears all shapes from the video.
     */
    abstract public void clearShape();

    /**
     * Clears all shapes from the video.
     */
    abstract public void clearDrawing();

    /**
     * Sets the repeat value.
     *
     * @param repeat if set to true repeating is enabled.
     */
    public void setRepeat(Boolean repeat) {
        this.repeat = repeat;
    }

    /**
     * Returns the repeat value.
     *
     * @return true if repeating is enabled.
     */
    public boolean isRepeat() {
        return repeat;
    }

    // vlcj event handling
    /**
     * Handles mediaChange event
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param l {@link libvlc_media_t}
     * @param string New media.
     */
    @Override
    public void mediaChanged(MediaPlayer mp, libvlc_media_t l, String string) {
    }

    /**
     * Handles opening event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void opening(MediaPlayer mp) {
    }

    /**
     * Handles buffering event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param f buffering status as {@link flaot}
     */
    @Override
    public void buffering(MediaPlayer mp, float f) {
    }

    /**
     * Handles playing event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void playing(MediaPlayer mp) {
    }

    /**
     * Handles pause event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void paused(MediaPlayer mp) {
//        if (controls != null) {
//            controls.highlightPause();
//        }
    }

    /**
     * Handles forward event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void forward(MediaPlayer mp) {
    }

    /**
     * Handles backward event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void backward(MediaPlayer mp) {
    }

    /**
     * Handles timeChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param l time position in ms as {@link Long}
     */
    @Override
    public void timeChanged(MediaPlayer mp, long l) {
        if (renderer != null) {
            renderer.setTimecode(VideoUtils.getTimeCode(l) + "\t<BR>\t"
                    + getVideoProgress());
        }
    }

    /**
     * Handles positionChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param f time position in percentage as {@link Float}
     */
    @Override
    public void positionChanged(MediaPlayer mp, float f) {
        if (slider != null) {
            slider.setSlider(f);
        }
//        if (controls != null) {
//            controls.highlightPlay();
//        }
        isPlaying = true;
    }

    /**
     * Handles seekableChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void seekableChanged(MediaPlayer mp, int i) {
    }

    /**
     * Handles pausableChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void pausableChanged(MediaPlayer mp, int i) {
    }

    /**
     * Handles titleChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void titleChanged(MediaPlayer mp, int i) {
    }

    /**
     * Handles snapshotTaken event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param string target file as {@link String}
     */
    @Override
    public void snapshotTaken(MediaPlayer mp, String string) {
    }

    /**
     * Handles lengthChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param l length of video in ms as {@link Long}
     */
    @Override
    public void lengthChanged(MediaPlayer mp, long l) {
    }

    /**
     * Handles videoOutput event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void videoOutput(MediaPlayer mp, int i) {
    }

    /**
     * Handles error event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void error(MediaPlayer mp) {
        log.error(mp + " has thrown an error.");
    }

    /**
     * Handles the mediaMetaChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void mediaMetaChanged(MediaPlayer mp, int i) {
    }

    /**
     * Handles the mediaSubItemAdded event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param l {@link libvlc_media_t}
     */
    @Override
    public void mediaSubItemAdded(MediaPlayer mp, libvlc_media_t l) {
    }

    /**
     * Handles the mediaDurationChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param l media duration in ms as {@link long}
     */
    @Override
    public void mediaDurationChanged(MediaPlayer mp, long l) {
    }

    /**
     * Handles mediaParsedChange event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void mediaParsedChanged(MediaPlayer mp, int i) {
    }

    /**
     * Handles meduaFreed event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void mediaFreed(MediaPlayer mp) {
    }

    /**
     * Handles mediaStageChanged event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void mediaStateChanged(MediaPlayer mp, int i) {
    }

    /**
     * Handles newMedia event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void newMedia(MediaPlayer mp) {
    }

    /**
     * Handles subItemPlayed event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void subItemPlayed(MediaPlayer mp, int i) {
    }

    /**
     * Handles subItemFinished event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     * @param i status as {@link Integer}
     */
    @Override
    public void subItemFinished(MediaPlayer mp, int i) {
    }

    /**
     * Handles endOfSubItems event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void endOfSubItems(MediaPlayer mp) {
    }

    /**
     * Handles finished event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void finished(MediaPlayer mp) {
        isPlaying = false;
        if (mediaPlayer == null) {
            return;
        }
        if (isRepeat()) {
            mediaPlayer.playMedia(getSource());
        }
    }

    /**
     * Handles stop event.
     *
     * @param mp {@link MediaPlayer} which fired the event
     */
    @Override
    public void stopped(MediaPlayer mp) {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.prepareMedia(getSource());
        slider.setSlider(0.f);
    }

    /**
     * Makes a snapshot of the video
     *
     * @return video snapshot as {@link BufferedImage}
     */
    abstract public BufferedImage getSnapshot();

    /**
     * Saves the video frame including the shape to {@link File}
     * {@code source + "."+ mediaPlayer.getTime() + ".png"}
     */
    abstract public void saveShape();

    /**
     * Adds the {@link Point} to the {@link List} which should be draw on the
     * video.
     *
     * @param point {@link Point}
     */
    abstract public void draw(Point point);

    /**
     * Renturns the video as {@link BufferedImage}
     *
     * Note that this method only works of its a {@link RenderedVideoHandler}
     * otherwise this method returns a snapshot of the video as
     * {@link BufferedImage}
     *
     * @return {@link BufferedImage}
     */
    abstract public BufferedImage getVideoImage();

    /**
     * Enables / Disables the title overlay.
     *
     * @param enabled if true title overlay will be enabled.
     */
    abstract public void setTitleOverlayEnabled(boolean enabled);

    /**
     * Enables the time overlay for {@code timeout} ms.
     *
     * @param timeout ms how long overlay will be shown
     */
    abstract public void enableTimeCodeOverlay(long timeout);

    /**
     * Returns the video title.
     *
     * @return {@link String}
     */
    abstract public String getTitle();

    /**
     * Method to sets detected handwritting to the video handler.
     *
     * @param hwr
     */
    abstract public void setHWR(String hwr);
}
