/*
 * NativeInterface.java
 * 
 * Copyright (c) 2012, Markus Weber All rights reserved.
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
package de.dfki.covida.nativealgorithm;

import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.Stroke;
import de.dfki.covida.covidacore.data.StrokeList;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NativeInterface.
 *
 * @author Markus Weber
 *
 */
public class NativeInterface {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(NativeInterface.class);
    // Load library

    static {
        initGuard = new Object();
        try {
            System.loadLibrary("CovidaNativeInterface");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("[ERROR] : " + e.getMessage());
        }
    }

    /**
     * Startup of native interface
     */
    private static void startupNative() {
        synchronized (initGuard) {
            if (!loaded) {
                loaded = true;
                ShutdownHook shutdownHook = new ShutdownHook();
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }
        }
    }

    private native void onVideoOpened(final String path);

    private native void onVideoClosed(final String path);

    private native void onImageOpened(final String path);

    private native void onImageClosed(final String path);

    private native void circleAnnotation(final String path, final String description, final long start, final long stop,
            final float dx, final float dy, final float radius,
            final Point[] points);

    private native void lineAnnotation(final String path, final String description, final long start, final long stop,
            final float bx, final float by, final float ex, final float ey,
            final Point[] points);

    private native void polygonAnnotation(final String path, final String description, final long start, final long stop,
            final Point[] points);

    /**
     * Event if the video opened.
     *
     * @param path
     */
    public void onVideoOpened(final File path) {
        if (path.exists() && path.isFile()) {
            log.debug("Video opened event: {}", path.toString());
            onVideoOpened(path.getAbsoluteFile().toString());
        }
    }

    public void onVideoClosed(final File path) {
        if (path.exists() && path.isFile()) {
            log.debug("Video opened event: {}", path.toString());
            onVideoClosed(path.getAbsoluteFile().toString());
        }
    }

    public void onImageOpened(final File path) {
        if (path.exists() && path.isFile()) {
            log.debug("Image opened event: {}", path.toString());
            onImageOpened(path.getAbsoluteFile().toString());
        }
    }

    public void onImageClosed(final File path) {
        if (path.exists() && path.isFile()) {
            log.debug("Image closed event: {}", path.toString());
            onImageClosed(path.getAbsoluteFile().toString());
        }
    }

    /**
     * Converts stroke list to an array of points
     *
     * @param strokelist
     * @return
     */
    private Point[] toArray(StrokeList strokelist) {
        List<Point> points = new ArrayList<>();
        for (Stroke s : strokelist.strokelist) {
            points.addAll(s.points);
        }
        Point[] arr = new Point[points.size()];
        points.toArray(arr);
        return arr;
    }

    /**
     * shutdown hook
     */
    static class ShutdownHook extends Thread {

        @Override
        public void run() {
            shutdownNative();
        }
    }

    /**
     * Deregister native listeners.
     */
    private static void shutdownNative() {
    }
    /**
     * Singleton instance
     */
    private static NativeInterface instance;
    /**
     * Init guard
     */
    private static final Object initGuard;
    /**
     * Flag if native library is loaded.
     */
    private static boolean loaded;

    /**
     * New annotation send to native interface.
     *
     * @param f
     * @param a
     */
    public void newAnnotation(File f, Annotation a) {
        if (f.exists()) {
            switch (a.shapeType) {
                case CIRCLE:
                    circleAnnotation(f.getAbsolutePath(), a.description, a.time_start, a.time_end, 0.f, 0.f, 0.f, toArray(a.strokelist));
                case ELLIPSE:
                    
                case LINE:
                    lineAnnotation(f.getAbsolutePath(), a.description, a.time_start, a.time_end, 0f, 0f, 0f, 0f, toArray(a.strokelist));
                case POLYGON:
                    polygonAnnotation(f.getAbsolutePath(), a.description, a.time_start, a.time_end, toArray(a.strokelist));
                case RECTANGLE:
            }
        }
    }

    /**
     * Constructor
     *
     * @param conf
     * @throws Exception
     */
    private NativeInterface() {
    }

    /**
     * Singleton interface for Windows 7 Input.
     *
     * @param conf
     * @return
     * @throws Exception
     */
    public static NativeInterface getInstance() {
        if (instance == null) {
            startupNative();
            instance = new NativeInterface();
        }
        return instance;
    }
}
