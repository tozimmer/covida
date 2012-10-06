/*
 * CovidaApplicationPreloader.java
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
package de.dfki.covida.visualjme2;

import com.jme.math.Vector3f;
import com.jme.util.GameTaskQueueManager;
import de.dfki.covida.covidacore.data.VideoFormat;
import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.videovlcj.preload.VideoPreload;
import de.dfki.covida.visualjme2.components.ControlButton;
import de.dfki.covida.visualjme2.components.annotation.AnnotationClipboard;
import de.dfki.covida.visualjme2.components.annotation.AnnotationSearchField;
import de.dfki.covida.visualjme2.components.video.VideoComponent;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.CovidaRootNode;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * CovidaApplicationPreloader
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class CovidaApplicationPreloader implements Runnable {

    private CovidaApplication application;
    private Logger log = Logger.getLogger(CovidaApplicationPreloader.class);
    private List<VideoPreload> preloadVideos;
    private List<VideoFormat> videoFormats;
    private List<VideoComponent> videos;

    public CovidaApplicationPreloader(CovidaApplication application) {
        this.application = application;
        this.preloadVideos = new ArrayList<>();
        this.videoFormats = new ArrayList<>();
        this.videos = new ArrayList<>();
    }

    private void preloadVideo(String source) {
        VideoPreload preload = new VideoPreload(source);
        preload.run();
        preloadVideos.add(preload);
    }

    private void createVideoInstances(List<VideoMediaData> videoData) {
        for (int i = 0; i < videoData.size(); i++) {
            VideoComponent video = new VideoComponent(
                    videoData.get(i).videoSource, videoData.get(i).videoName,
                    250, videoFormats.get(i));
            videos.add(video);
            GameTaskQueueManager.getManager().update(new AttachChildCallable(CovidaRootNode.node, video.node));
            video.createControls();
            video.createVideo();
            video.createFields();
            video.createOverlays();
            video.startTests();
        }
        for (VideoComponent video : videos) {
            while (!video.isReady()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        }
    }

    private void createSideMenus() {
        AnnotationClipboard clipboard = new AnnotationClipboard(
                "media/textures/clipboard_field_color.png",
                application.getWidth() / 2, (int) (application.getHeight() / 1.5f));
        ControlButton clipboardButton = new ControlButton(ActionName.CLOSE,
                clipboard, "media/textures/arrow.png",
                "media/textures/arrow.png", 64, 64);
        clipboard.open();
        clipboard.setLocalTranslation(clipboard.getLocalTranslation().x + clipboard.getWidth()/2, 
                clipboard.getLocalTranslation().y, 0);
        application.addComponent(clipboardButton);
        AnnotationSearchField search = new AnnotationSearchField(
                "media/textures/search_field_color.png",
                application.getWidth() / 2, (int) (application.getHeight() / 1.5f));
        ControlButton searchButton = new ControlButton(ActionName.CLOSE,
                search, "media/textures/search.png",
                "media/textures/search.png", 64, 64);
        search.rotate(3.75f, new Vector3f(0, 0, 1));
        search.setLocalTranslation(search.getLocalTranslation().x, 
                search.getLocalTranslation().y + application.getHeight(), 0);
        search.setDefaultPosition();
        search.open();
        application.addComponent(searchButton);
        AnnotationClipboard clipboard2 = new AnnotationClipboard(
                "media/textures/clipboard_field_color.png",
                application.getWidth() / 2, (int) (application.getHeight() / 1.5f));
        ControlButton clipboardButton2 = new ControlButton(ActionName.CLOSE,
                clipboard2, "media/textures/arrow.png",
                "media/textures/arrow.png", 64, 64);
        application.addComponent(clipboardButton2);
        AnnotationSearchField search2 = new AnnotationSearchField(
                "media/textures/search_field_color.png",
                application.getWidth() / 2, (int) (application.getHeight() / 1.5f));
        ControlButton searchButton2 = new ControlButton(ActionName.CLOSE,
                search2, "media/textures/search.png",
                "media/textures/search.png", 64, 64);
        application.addComponent(searchButton2);
    }

    public void cleanUp() {
        for (VideoPreload preload : preloadVideos) {
            preload.cleanUp();
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName() + " Thread");
        List<VideoMediaData> videoData = application.getVideoSources();
        for (VideoMediaData videoDatum : videoData) {
            preloadVideo(videoDatum.videoSource);
        }
        for (VideoPreload preload : preloadVideos) {
            while (preload.getVideoDimension() == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
            log.debug("Detected dimension: " + preload.getVideoDimension());
            Dimension dimension = preload.getVideoDimension();
            videoFormats.add(new VideoFormat((float) dimension.width
                    / (float) dimension.height));
        }
        log.info("Temp videos (" + videoData.size() + ") "
                + "loaded and video dimensions detected ("
                + videoFormats.size() + ")");
        createVideoInstances(videoData);
        log.info("Initialization complete");
        application.endLoadingAnimation();
        for (VideoComponent video : videos) {
            application.addComponent(video);
        }
        createSideMenus();
    }
}
