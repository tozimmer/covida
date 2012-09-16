/*
 * ITouchAndWriteComponent.java
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

import de.dfki.touchandwrite.analyser.touch.gestures.events.PanEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEventImpl;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import java.awt.Dimension;

/**
 * ITouchAndWriteComponent
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public interface ITouchAndWriteComponent {
    
    /**
     * Returns the {@link Dimension} of the component.
     * 
     * @return the {@link Dimension} of the component.
     */
    public Dimension getDimension();
    
    /**
     * Returns display size
     * 
     * @return display size
     */
    public Dimension getDisplaySize();
    
    /**
     * Returns the rotation angle as {@link Float}.
     * 
     * @return the rotation angle as {@link Float}.
     */
    public float getRotationAngle();
    
    /**
     * Returns the x coordinate on screen as {@link Integer} of the center 
     * of this component.
     * 
     * @return the x coordinate on screen as {@link Integer} of the center 
     * of this component.
     */
    public int getPosX();
    
    /**
     * Returns the y coordinate on screen as {@link Integer} of the center 
     * of this component.
     * 
     * @return the y coordinate on screen as {@link Integer} of the center 
     * of this component.
     */
    public int getPosY();
    
    /**
     * Returns {@code true} if this component is always on top
     * 
     * @return true if this component is always on top
     */
    public boolean isAlwaysOnTop();
    
    /**
     * Method which determines if {@code x} and {@code y} is in the
     * bounding box of the component.
     * 
     * @param x x coordinate on the display as {@link Integer}
     * @param y y coordinate on the display as {@link Integer}
     * @return true if x,y is in area of the component
     */
    public boolean inArea(int x, int y);
    
    /**
     * Returns the z position of the component on the display
     * 
     * @return z position as {@link Integer}
     */
    public int getZPosition();
    
    /**
     * Method which handles incoming touch dead events.
     * 
     * @param id touch id as {@link Integer}
     * @param x x coordinate on the display as {@link Integer}
     * @param y y coordinate on the display as {@link Integer}
     */
    public void touchDeadAction(int id, int x, int y);
    
    /**
     * Method which handles incoming touch alive events.
     * 
     * @param id touch id as {@link Integer}
     * @param x x coordinate on the display as {@link Integer}
     * @param y y coordinate on the display as {@link Integer}
     */
    public void touchAliveAction(int id, int x, int y);
    
    /**
     * Method which handles incoming touch virth events.
     * 
     * @param id touch id as {@link Integer}
     * @param x x coordinate on the display as {@link Integer}
     * @param y y coordinate on the display as {@link Integer}
     */
    public void touchBirthAction(int id, int x, int y);
    
    /**
     * Method to set {@link ITouchAndWriteComponent} to the front of the display.
     */
    public void toFront();
    
    /**
     * Abstract method for incomming drag events.
     * 
     * @param id id of the touch
     * @param x x coordinate from the origin
     * @param y y coordinate from the origin
     * @param dx delta x from the drag translation
     * @param dy delta y from the drag translation
     */
    public void dragAction(int id, int x, int y, int dx, int dy);
    
    /**
     * Abstract method for incomming drag events.
     * 
     * @param id id of the touch
     * @param x x coordinate from the origin
     * @param y y coordinate from the origin
     * @param dx delta x from the drag translation
     * @param dy delta y from the drag translation
     */
    public void dragEndAction(int id, int x, int y, int dx, int dy);
    
    /**
     * Abstract method for incomming hwr results.
     * 
     * @param hwr HWR result as {@link String}
     */
    public void hwrAction(String hwr);
    
    /**
     * Abstract method for incomming shape events
     * 
     * @param event  {@link ShapeEvent}
     */
    public void onShapeEvent(ShapeEvent event);
    
    /**
     * Abstract method for drawing incoming pen events
     * 
     * @param x x coordinate on the display as {@link Integer}
     * @param y y coordinate on the display as {@link Integer} 
     */
    public void draw(int x, int y);
    
    /**
     * Abstract method for incomming rotate events
     * 
     * @param event {@link RotationGestureEventImpl} 
     */
    public void rotateAction(RotationGestureEventImpl event);
    
    /**
     * Abstract method for incomming zoom events
     * 
     * @param event {@link ZoomEventImpl}
     */
    public void zoomAction(ZoomEventImpl event);
    
    /**
     * Abstract method for incomming pan events
     * 
     * @param event {@link PanEventImpl}
     */
    public void panAction(PanEventImpl event);
}
