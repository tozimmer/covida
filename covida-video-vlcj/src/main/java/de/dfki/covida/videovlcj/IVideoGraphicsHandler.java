/*
 * IVideoGraphicsHandler.java
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

import de.dfki.covida.covidacore.data.Stroke;
import de.dfki.covida.covidacore.data.StrokeList;
import java.awt.Point;
import java.awt.Polygon;
import java.util.List;


/**
 * Interface for video graphics handler.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public interface IVideoGraphicsHandler {
 
    /**
     * Returns the drawing as {@link List} of {@link List} of {@link Point}
     * 
     * @return {@link List} of {@link StrokeList}
     */
    public List<Stroke> getDrawings();

    /**
     * Clears all shapes
     */
    public void clearShapes();

    /**
     * Returns the shape as {@link List} of {@link List} of {@link Point}
     * 
     * @return {@link List} of {@link StrokeList}
     */
    public StrokeList getSavedShapes();

    /**
     * Adds the shape as {@link List} of {@link Point}
     * 
     * @param points {@link List} of {@link Point}
     */
    public void addShape(Stroke stroke);

    /**
     * Sets the handwritting
     * 
     * @param hwr {@link String}
     */
    public void setHWR(String hwr);

    /**
     * Clears all drawings
     */
    public void clearDrawing();

    public void clear();

    public void setTimecode(String string);
    
}
