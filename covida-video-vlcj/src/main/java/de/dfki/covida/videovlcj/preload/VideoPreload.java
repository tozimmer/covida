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

import de.dfki.covida.videovlcj.rendered.VideoRenderer;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

/**
 * Component to preload videos for dimension detection
 *
 * @author Tobias Zimmermann
 *
 */
public class VideoPreload implements Runnable, MediaPlayerEventListener {

    /**
     * Logger
     */
    private Logger log = Logger.getLogger(VideoPreload.class);
    private final String videoSource;
    private Dimension dimension;
    BufferedImage frame;
    private MediaPlayer mediaPlayer;
    private VideoRenderer renderer;

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
    public VideoPreload(String source) {
        dimension = null;
        videoSource = source;
    }

    private void initComponent() {
        log.debug("VIDEO SOURCE (PRELOAD): " + this.videoSource);
        this.renderer = new VideoRenderer(1, 1, "");
        mediaPlayer = (new MediaPlayerFactory()).newDirectMediaPlayer(1, 1, renderer);
        mediaPlayer.addMediaPlayerEventListener(this);
        mediaPlayer.playMedia(videoSource);
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

    public void mediaChanged(MediaPlayer mp, libvlc_media_t l, String string) {
    }

    public void opening(MediaPlayer mp) {
    }

    public void buffering(MediaPlayer mp, float f) {
    }

    public void playing(MediaPlayer mp) {
    }

    public void paused(MediaPlayer mp) {
    }

    public void stopped(MediaPlayer mp) {
    }

    public void forward(MediaPlayer mp) {
    }

    public void backward(MediaPlayer mp) {
    }

    public void finished(MediaPlayer mp) {
    }

    public void timeChanged(MediaPlayer mp, long l) {
    }

    public void positionChanged(MediaPlayer mp, float f) { 
        dimension = mediaPlayer.getVideoDimension();
    }

    public void seekableChanged(MediaPlayer mp, int i) {
    }

    public void pausableChanged(MediaPlayer mp, int i) {
    }

    public void titleChanged(MediaPlayer mp, int i) {
    }

    public void snapshotTaken(MediaPlayer mp, String string) {
    }

    public void lengthChanged(MediaPlayer mp, long l) {
    }

    public void videoOutput(MediaPlayer mp, int i) {
    }

    public void error(MediaPlayer mp) {
    }

    public void mediaMetaChanged(MediaPlayer mp, int i) {
    }

    public void mediaSubItemAdded(MediaPlayer mp, libvlc_media_t l) {
    }

    public void mediaDurationChanged(MediaPlayer mp, long l) {
    }

    public void mediaParsedChanged(MediaPlayer mp, int i) {
    }

    public void mediaFreed(MediaPlayer mp) {
    }

    public void mediaStateChanged(MediaPlayer mp, int i) {
    }

    public void newMedia(MediaPlayer mp) {
    }

    public void subItemPlayed(MediaPlayer mp, int i) {
    }

    public void subItemFinished(MediaPlayer mp, int i) {
    }

    public void endOfSubItems(MediaPlayer mp) {
    }
}
