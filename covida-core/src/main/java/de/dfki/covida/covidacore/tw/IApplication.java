/*
 * IApplication.java
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
package de.dfki.covida.covidacore.tw;

import de.dfki.covida.covidacore.data.ImageMediaData;
import de.dfki.covida.covidacore.data.VideoMediaData;
import java.awt.Dimension;

/**
 * Interface of a covida application
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public interface IApplication {

    /**
     * Returns the window title application of the application.
     *
     * @return window title as {@link String}
     */
    public String getWindowTitle();

    /**
     * Starts the application
     */
    public void start();

    /**
     * Returns true if the application is ready.
     *
     * @return true if application is ready
     */
    public boolean isReady();
    
    public void login(String id, int x, int y, String login);
    
    public Dimension getScreenSize();
    
    public void draw(String id, int x, int y, boolean penUp);
    
    public void clearDrawings();
    
    public void addVideo(VideoMediaData data);
    
    public void addImage(ImageMediaData data);

    public void close();
}
