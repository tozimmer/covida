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
import de.dfki.covida.covidacore.components.IVideoComponent;
import de.dfki.covida.covidacore.data.Stroke;
import de.dfki.covida.covidacore.data.StrokeList;
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.covidacore.utils.VideoUtils;
import de.dfki.covida.videovlcj.preload.VideoPreload;
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
    private static final String[] VLC_ARGS = {
        "--intf", "dummy", /* no interface */
        "--vout", "dummy", /* we don't want video (output) */
        "--no-video-title-show", /* nor the filename displayed */
        "--no-stats", /* no stats */ //        
        "--no-sub-autodetect-file", /* we don't want subtitles */
        "--no-disable-screensaver", /* we don't want interfaces */
        "--no-snapshot-preview", /* no blending in dummy vout */
        "--ffmpeg-threads", "0"
    };
    private static final String[] VLC_ARGS_MAC = {
        "--intf", "dummy", /* no interface */
        "--vout", "dummy", /* we don't want video (output) */
        "--no-video-title-show", /* nor the filename displayed */
        "--no-stats", /* no stats */ //        
        "--no-sub-autodetect-file", /* we don't want subtitles */
        "--no-disable-screensaver", /* we don't want interfaces */
        "--no-snapshot-preview", /* no blending in dummy vout */
        "--ffmpeg-threads", "0"
    };
    /**
     * Video Slider {@link ISlider}
     */
    protected ISlider slider;
    /**
     * Video controls {@link IVideoControls}
     */
    protected IVideoControls controls;
    /**
     * Current recognized handwriting Note that this {@link String} must be set
     * by the visual component of covida
     */
    protected String hwr;
    /**
     * {@link MediaPlayer} instance to play the {@code source}
     */
    protected MediaPlayer mediaPlayer;

    protected IVideoGraphicsHandler graphics;
    /**
     * The {@link MediaPlayerFactory} to crate {@link VideoSurface} or
     * {@link MediaPlayer}
     */
    protected MediaPlayerFactory mediaPlayerFactory;
    /**
     * Corresponding {@link IVideoComponent}
     */
    private final IVideoComponent video;
    /**
     * Preload component for determining video dimensions
     */
    private VideoPreload preload;
    private final VideoMediaData data;

    /**
     * Creates an instance of {@link AbstractVideoHandler}
     *
     * @param data {@link VideoMediaData}
     * @param video corresponding {@link IVideoComponent}
     */
    public AbstractVideoHandler(VideoMediaData data, IVideoComponent video) {
        this.data = data;
        this.video = video;
    }

    public void initComponent() {
        if (data.width < 1 || data.height < 1) {
            preload();
        } else {
            create(data.width, data.height);
        }
    }

    /**
     * Creates a {@link VideoPreload} instance to determine video dimensions.
     *
     * Note that the {@link VideoPreload} instance will call
     * {@link #create(int, int)} method if dimension is determined.
     */
    private void preload() {
        preload = new VideoPreload(data, this);
        Thread preloadThread = new Thread(preload);
        preloadThread.setName(data.videoName + " preload");
        preloadThread.start();
    }

    /**
     * Creates the video video player
     *
     * @param width width of the video player
     * @param height height of the video player
     */
    public final void create(int width, int height) {
        data.width = width;
        data.height = height;
        String[] args;
        if (Platform.isMac()) {
            args = VLC_ARGS_MAC;
        } else {
            args = VLC_ARGS;
        }
        this.mediaPlayerFactory = new MediaPlayerFactory(args);
        graphics = new VideoRenderer(data.width, data.height, data.videoName);
        mediaPlayer = mediaPlayerFactory.newDirectMediaPlayer(width, height,
                (VideoRenderer) graphics);
        addEventListener();
        video.create();
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
     * Returns the current time postion in milliseconds.
     *
     * Note that only the part from {@code timeStart} to {@code timeEnd} is
     * considered.
     *
     * @see #setTimeRange(long, long)
     *
     * @return current time stamp in milliseconds
     */
    public long getTime() {
        if (mediaPlayer == null) {
            return -1;
        }
        if (data.time_start > 0) {
            if (isReady()) {
                return mediaPlayer.getTime() - data.time_start;
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
     * Start video
     */
    public void open() {
        if (mediaPlayer == null) {
            log.warn("Could not start video, mediaPlayer == null!");
            return;
        }
        mediaPlayer.prepareMedia(getSource());
        mediaPlayer.setPlaySubItems(true);
        mediaPlayer.setVolume(0);
        controls.highlightPlay();
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
     * @return {@link List} of {@link StrokeList}
     */
    abstract public StrokeList getShapes();

    /**
     * Returns drawing points.
     *
     * @return {@link List} of {@link Stroke}
     */
    abstract public List<Stroke> getDrawings();

    /**
     * Sets the shape points to draw on the video.
     *
     * @param stroke {@link Stroke}
     */
    abstract public void addShape(Stroke stroke);

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

    /**
     * Returns playing status.
     *
     * @return true if video currently playing
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Returns volume as percent.
     *
     * @return {@link Integer}
     */
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
    }

    /**
     * Stops the video.
     */
    public void stop() {
        controls.highlightStop();
        controls.highlightPlay();
        if (mediaPlayer == null) {
            return;
        }
        if (isReady()) {
            setTimePostion(0);
            mediaPlayer.stop();
        }
        graphics.clear();
        slider.detach();
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
            }
        }
        controls.highlightPlay();
    }

    /**
     * Resumes the video.
     */
    public void resume() {
        if (mediaPlayer == null) {
            return;
        }
        if (isReady()) {
            if (!mediaPlayer.isPlaying()) {
                this.mediaPlayer.play();
                controls.highlightPause();
            }
            slider.attach();
        }
    }

    /**
     * Sets the time position of the video in percentage.
     *
     * Note that only the part from {@code timeStart} to {@code timeEnd} is
     * considered if {@code isTimeRanged} is {@code true}.
     *
     * @see #setTimeRange(long, long)
     *
     * @param time
     */
    public void setTimePosition(long time) {
        if (mediaPlayer == null) {
            return;
        }
        if (data.time_start > 0) {
            if (isReady()) {
                mediaPlayer.setTime(time + data.time_start);
            }
        } else {
            if (isReady()) {
                mediaPlayer.setTime(time);
            }
        }
        mediaPlayer.nextFrame();
    }

    /**
     * Sets the time position of the video in percentage.
     *
     * Note that only the part from {@code timeStart} to {@code timeEnd} is
     * considered.
     *
     * @see #setTimeRange(long, long)
     *
     * @param percentage
     */
    public void setTimePostion(float percentage) {
        if (mediaPlayer == null && mediaPlayer.isSeekable()) {
            return;
        }
        if (data.time_start > 0) {
            if (isReady()) {
                mediaPlayer.setTime((long) ((percentage * (getMaxTime()
                        - data.time_start)) + data.time_start));
            }
        } else {
            if (isReady()) {
                mediaPlayer.setTime((long) (percentage * getMaxTime()));
            }
        }
        if (!isPlaying()) {
            slider.setSlider(percentage);
            int perc = (int) (percentage * 100);
            graphics.setTimecode(VideoUtils.getTimeCode(
                    (long) (percentage * getMaxTime()))
                    + "\t<BR>\t" + String.valueOf(perc) + " %");
        }
    }

    /**
     * Returns the video source as {@link String}.
     *
     * @return video source as {@link String}
     */
    public String getSource() {
        return data.videoSource;
    }

    /**
     * Returns the video width as {@link Integer}.
     *
     * @return video width as {@link Integer}
     */
    public int getWidth() {
        return data.width;
    }

    /**
     * Returns the video height as {@link Integer}.
     *
     * @return video height as {@link Integer}
     */
    public int getHeight() {
        return data.height;
    }

    /**
     * Returns the max time position of the video in ms.
     *
     * Note that only the part from {@code timeStart} to {@code timeEnd} is
     * considered.
     *
     * @see #setTimeRange(long, long)
     *
     * @return max time position
     */
    public long getMaxTime() {
        if (mediaPlayer == null) {
            return -1;
        }
        if (data.time_end > 0) {
            if (isReady()) {
                return data.time_end;
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
        data.time_start = start;
        data.time_end = end;
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
        data.repeat = repeat;
    }

    /**
     * Returns the repeat value.
     *
     * @return true if repeating is enabled.
     */
    public boolean isRepeat() {
        return data.repeat;
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
     * @param f buffering status as {@link Float}
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
        if (controls != null) {
            controls.highlightPause();
        }
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
        int perc = (int) (f * 100);
        graphics.setTimecode(VideoUtils.getTimeCode((long) (f * getMaxTime()))
                + "\t<BR>\t" + String.valueOf(perc) + " %");
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
     * @param l media duration in ms as {@link Long}
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
        controls.highlightPlay();
        if (mediaPlayer == null) {
            return;
        }
        if (isRepeat()) {
            mediaPlayer.playMedia(getSource());
            controls.highlightPause();
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
    abstract public void saveAnnotatedFrame();

    /**
     * Adds the {@link Point} to the {@link List} which should be draw on the
     * video.
     *
     * @param point {@link Point}
     */
    abstract public void draw(Point point);

    abstract public void endDrawStroke();

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

    public void setShapes(List<Stroke> drawings) {
        for (Stroke shape : drawings) {
            addShape(shape);
        }
    }
}
