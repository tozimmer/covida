/*
 * ApplicationImpl.java
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

import de.dfki.covida.covidacore.data.VideoMediaData;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import java.awt.Dimension;

/**
 * Application Implementation
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ApplicationImpl extends AbstractApplication {

    /**
     * {@link TouchAndWriteDevice}
     */
    private final TouchAndWriteDevice device;
    protected boolean ready;

    /**
     * Creates an instance of {@link ApplicationImpl}
     *
     * @param device {@link TouchAndWriteDevice}
     * @param windowtitle window title for the application
     */
    public ApplicationImpl(TouchAndWriteDevice device, String windowtitle) {
        super();
        this.device = device;
        this.windowtitle = windowtitle;
    }

    /**
     * Loading animation
     *
     * Note that method have to be overridden for a loading animation.
     */
    protected void loadingAnimation() {
    }

    @Override
    protected void simpleInitGame() {
        loadingAnimation();
        ready = true;
    }

    @Override
    public void setBackground() {
    }

    @Override
    public String getWindowTitle() {
        return windowtitle;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public Dimension getScreenSize() {
        return new Dimension(display.getWidth(), display.getHeight());
    }

    @Override
    public void login(String id, int x, int y, String login) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void draw(String id, int x, int y, boolean penUp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addVideo(VideoMediaData data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clearDrawings() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
