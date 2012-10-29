/*
 * VideoPreload.java
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
package de.dfki.covida.videovlcj.preload;

import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.videovlcj.AbstractVideoHandler;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

/**
 * Component to preload videos for dimension detection
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class VideoPreload implements Runnable, MediaPlayerEventListener {
    
    private final CountDownLatch inPositionLatch = new CountDownLatch(1);
    private static float[] VLC_THUMBNAIL_POSITION;
    private int vlc_thumbnail_number = 0;
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(VideoPreload.class);
    private Dimension dimension;
    BufferedImage frame;
    private MediaPlayer mediaPlayer;
    private AbstractVideoHandler video;
    private static final String[] VLC_ARGS = {
        "--intf", "dummy", /* no interface */
        "--vout", "dummy", /* we don't want video (output) */
//        "--no-audio", /* we don't want audio (decoding) */ //        
//        "--no-video-title-show", /* nor the filename displayed */
//        "--no-stats", /* no stats */ //        
//        "--no-sub-autodetect-file", /* we don't want subtitles */
//        "--no-disable-screensaver", /* we don't want interfaces */
//        "--no-snapshot-preview", /* no blending in dummy vout */
//        "--ffmpeg-threads", "0"
    };
    private MediaPlayerFactory factory;
    private final VideoMediaData data;
    private final boolean thumbcreation;

    /**
     * Creates a new intance of {@link VideoPreload}
     *
     * @param data video data {@link VideoMediaData}
     * @param video {@link AbstractVideoHandler} which should be called if
     * preload is complete
     */
    public VideoPreload(VideoMediaData data, AbstractVideoHandler video) {
        this.video = video;
        dimension = null;
        this.data = data;
        int thumbcount = CovidaConfiguration.getInstance().thumbcount;
        float step = (100.f / ((float) thumbcount + 1)) / 100.f;
        float position = step;
        if (data.thumbs.size() != thumbcount) {
            data.thumbs.clear();
            thumbcreation = true;
            VLC_THUMBNAIL_POSITION = new float[thumbcount];
            for (int i = 0; i < thumbcount; i++) {
                VLC_THUMBNAIL_POSITION[i] = position;
                position += step;
            }
        } else {
            thumbcreation = false;
        }
    }
    
    public VideoPreload(VideoMediaData data) {
        this(data, null);
    }

    /**
     * Initializes the preload
     */
    private void initComponent() {
        log.debug("VIDEO SOURCE (PRELOAD): " + data.videoSource);
        factory = new MediaPlayerFactory(VLC_ARGS);
        mediaPlayer = factory.newHeadlessMediaPlayer();
        mediaPlayer.setPlaySubItems(true);
        mediaPlayer.addMediaPlayerEventListener(this);
        mediaPlayer.setVolume(0);
        if (mediaPlayer.startMedia(data.videoSource)) {
            if (thumbcreation && mediaPlayer.isSeekable()) {
                log.debug("Create thumbnails for video: " + data.videoName);
                while (vlc_thumbnail_number < VLC_THUMBNAIL_POSITION.length) {
                    mediaPlayer.setPosition(VLC_THUMBNAIL_POSITION[vlc_thumbnail_number]);
                    try {
                        inPositionLatch.await(); // Might wait forever if error
                    } catch (InterruptedException ex) {
                        log.error("", ex);
                    }
                    int ratio = (int) ((float) data.width / (float) data.height);
                    BufferedImage image = mediaPlayer.getSnapshot(128, (int) (ratio * 128));
                    if (image != null) {
                        data.thumbs.add(image);
                    }
                    if (dimension == null) {
                        dimension = mediaPlayer.getVideoDimension();
                        data.height = dimension.height;
                        data.width = dimension.width;
                    }
                    vlc_thumbnail_number++;
                }
                mediaPlayer.stop();
                if (dimension == null) {
                    log.error("Video dimension detection failed!");
                } else if (video != null) {
                    video.create(dimension.width, dimension.height);
                }
            } else {
                while (dimension != null) {
                    try {
                        inPositionLatch.await(); // Might wait forever if error
                    } catch (InterruptedException ex) {
                        log.error("", ex);
                    }
                    if (dimension == null) {
                        log.error("Video dimension detection failed!");
                    } else if (video != null) {
                        video.create(dimension.width, dimension.height);
                    }
                }
            }
            mediaPlayer.release();
            factory.release();
            CovidaConfiguration.getInstance().save();
        }
    }

    /**
     * Returns video dimensions
     *
     * @return {@link Dimension}
     * @return {@code null} if dimension is not available
     */
    public Dimension getVideoDimension() {
        return dimension;
    }
    
    @Override
    public void run() {
        initComponent();
    }
    
    @Override
    public void mediaChanged(MediaPlayer mp, libvlc_media_t l, String string) {
    }
    
    @Override
    public void opening(MediaPlayer mp) {
    }
    
    @Override
    public void buffering(MediaPlayer mp, float f) {
    }
    
    @Override
    public void playing(MediaPlayer mp) {
    }
    
    @Override
    public void paused(MediaPlayer mp) {
    }
    
    @Override
    public void stopped(MediaPlayer mp) {
    }
    
    @Override
    public void forward(MediaPlayer mp) {
    }
    
    @Override
    public void backward(MediaPlayer mp) {
    }
    
    @Override
    public void finished(MediaPlayer mp) {
    }
    
    @Override
    public void timeChanged(MediaPlayer mp, long l) {
    }
    
    @Override
    public void positionChanged(MediaPlayer mp, float newPosition) {
        /* 90% margin */
        if (newPosition >= VLC_THUMBNAIL_POSITION[vlc_thumbnail_number] * 0.9f) {
            inPositionLatch.countDown();
        }
    }
    
    @Override
    public void seekableChanged(MediaPlayer mp, int i) {
    }
    
    @Override
    public void pausableChanged(MediaPlayer mp, int i) {
    }
    
    @Override
    public void titleChanged(MediaPlayer mp, int i) {
    }
    
    @Override
    public void snapshotTaken(MediaPlayer mp, String filename) {
    }
    
    @Override
    public void lengthChanged(MediaPlayer mp, long l) {
    }
    
    @Override
    public void videoOutput(MediaPlayer mp, int i) {
    }
    
    @Override
    public void error(MediaPlayer mp) {
        log.error("Error in "+mp.mrl());
    }
    
    @Override
    public void mediaMetaChanged(MediaPlayer mp, int i) {
    }
    
    @Override
    public void mediaSubItemAdded(MediaPlayer mp, libvlc_media_t l) {
        List<String> items = mediaPlayer.subItems();
        log.debug(Arrays.toString(items.toArray()));
    }
    
    @Override
    public void mediaDurationChanged(MediaPlayer mp, long l) {
    }
    
    @Override
    public void mediaParsedChanged(MediaPlayer mp, int i) {
    }
    
    @Override
    public void mediaFreed(MediaPlayer mp) {
    }
    
    @Override
    public void mediaStateChanged(MediaPlayer mp, int i) {
    }
    
    @Override
    public void newMedia(MediaPlayer mp) {
    }
    
    @Override
    public void subItemPlayed(MediaPlayer mp, int i) {
    }
    
    @Override
    public void subItemFinished(MediaPlayer mp, int i) {
    }
    
    @Override
    public void endOfSubItems(MediaPlayer mp) {
    }
}
