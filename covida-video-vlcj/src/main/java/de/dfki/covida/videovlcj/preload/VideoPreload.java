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

import de.dfki.covida.videovlcj.AbstractVideoHandler;
import de.dfki.covida.videovlcj.rendered.VideoRenderer;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
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

    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(VideoPreload.class);
    private final String videoSource;
    private Dimension dimension;
    BufferedImage frame;
    private MediaPlayer mediaPlayer;
    private VideoRenderer renderer;
    private final AbstractVideoHandler video;

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
    public VideoPreload(String source, AbstractVideoHandler video) {
        this.video = video;
        dimension = null;
        videoSource = source;
    }

    private void initComponent() {
        log.debug("VIDEO SOURCE (PRELOAD): " + this.videoSource);
        this.renderer = new VideoRenderer(1, 1, "");
        mediaPlayer = (new MediaPlayerFactory()).newDirectMediaPlayer(1, 1, renderer);
        mediaPlayer.addMediaPlayerEventListener(this);
        mediaPlayer.setVolume(0);
        mediaPlayer.playMedia(videoSource);
        mediaPlayer.setPlaySubItems(true);
    }

    @Override
    public void run() {
        initComponent();
    }

    public void cleanUp() {
        mediaPlayer.release();
    }

    public Dimension getVideoDimension() {
        return dimension;
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
    public void positionChanged(MediaPlayer mp, float f) {
        if (dimension == null) {
            dimension = mediaPlayer.getVideoDimension();
            video.create(dimension.width, dimension.height);
        } else {
            cleanUp();
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
    public void snapshotTaken(MediaPlayer mp, String string) {
    }

    @Override
    public void lengthChanged(MediaPlayer mp, long l) {
    }

    @Override
    public void videoOutput(MediaPlayer mp, int i) {
    }

    @Override
    public void error(MediaPlayer mp) {
    }

    @Override
    public void mediaMetaChanged(MediaPlayer mp, int i) {
    }

    @Override
    public void mediaSubItemAdded(MediaPlayer mp, libvlc_media_t l) {
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
