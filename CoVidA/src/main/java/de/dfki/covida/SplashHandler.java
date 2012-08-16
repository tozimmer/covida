/*
 * SplashHandler.java
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
package de.dfki.covida;

import org.apache.log4j.Logger;

public class SplashHandler implements Runnable {

    private CovidaBoard videoTouchBoard;
    private Logger log = Logger.getLogger(SplashHandler.class);

    public SplashHandler(CovidaBoard videoTouchBoard) {
        this.videoTouchBoard = videoTouchBoard;
    }

    public void run() {
        for (int i = 0; i < videoTouchBoard.getVideoCount(); i++) {
            new Thread(videoTouchBoard.preLoadVideo(i)).start();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            log.error(ex);
        }
        for (int i = 0; i < videoTouchBoard.getVideoCount(); i++) {
            while (!videoTouchBoard.getPreloadedVideo().get(i).isReady() || videoTouchBoard.getPreloadedVideo().get(i).getVideoDimension() == null) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
            log.debug("Detected dimension: " + videoTouchBoard.getPreloadedVideo().get(i).getVideoDimension());
            videoTouchBoard.videoDimensions.put(
                    videoTouchBoard.indexes.get(i),
                    videoTouchBoard.getPreloadedVideo().get(i).getVideoDimension());
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                log.error(e);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error(e);
            }
            log.debug("Preload " + i + 1 + " complete");
        }
        log.info("Temp videos loaded and video dimensions detected");
        log.info("videoTouchBoard.init()");
        for (int i = 0; i < videoTouchBoard.getVideoCount(); i++) {
            videoTouchBoard.getPreloadedVideo().get(i).cleanUp();
        }
        videoTouchBoard.init();
        while (!videoTouchBoard.isReady()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        log.info("Initialization complete");
        videoTouchBoard.removeController(0);
        videoTouchBoard.attachNodes();
        videoTouchBoard.registerPen();
        videoTouchBoard.registerTouch();
        videoTouchBoard.startVideos();
        while (!videoTouchBoard.isInitialized()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        // TODO preloading effects
        while (videoTouchBoard.getVideos().get(videoTouchBoard.getVideos().size() - 1).getVideoDimension() == null
                && videoTouchBoard.getVideos().get(videoTouchBoard.getVideos().size() - 1).getTime() > 10) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e);
        }
        videoTouchBoard.openOverlays();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e);
        }
        videoTouchBoard.closeOverlays();
    }
}
