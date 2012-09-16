/*
 * TouchAndWriteEventHandler.java
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

import de.dfki.covida.covidacore.utils.HWRPostProcessing;
import de.dfki.touchandwrite.ApplicationType;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.PanEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEventImpl;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEventImpl;
import de.dfki.touchandwrite.conf.TouchAndWriteConfiguration;
import de.dfki.touchandwrite.control.event.RegisterWindowEvent;
import de.dfki.touchandwrite.input.pen.data.PenEventDataType;
import de.dfki.touchandwrite.input.pen.event.GestureEvent;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.input.touch.event.TouchEvent;
import de.dfki.touchandwrite.input.touch.event.TouchState;
import de.dfki.touchandwrite.input.touch.gesture.TouchGestureEvent;
import de.dfki.touchandwrite.remote.RemoteTouchAndWriteApplication;
import de.dfki.touchandwrite.remote.event.HandwritingListener;
import de.dfki.touchandwrite.remote.event.TouchEventListener;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Touch&Write SDK event wrapper
 *
 * @author Tobias Zimmermann
 */
public class TouchAndWriteEventHandler extends RemoteTouchAndWriteApplication implements
        HandwritingListener, TouchEventListener {

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TouchAndWriteEventHandler.class);
    private static double MIN_ZOOM_DISTANCE = 150;
    private TouchAndWriteComponentHandler componentHandler;
    private final IApplication application;

    /**
     * Creates an instance of the TouchAndWriteEventHandler class wich wraps the
     * touch and pen events from the Touch and Write SDK
     *
     * @param mainFrame UI
     * @param wwPanel Visualization Panel
     */
    public TouchAndWriteEventHandler(IApplication application) {
        super(TouchAndWriteConfiguration.getDefaultEEESlateConfig());
        this.application = application;
    }

    public void start() {
        this.clientManager.addHWRListener(this);
        this.clientManager.addTouchListener(this);

        this.controlManager.triggerEvent(new RegisterWindowEvent(application.getWindowTitle()));
        this.componentHandler = TouchAndWriteComponentHandler.getInstance();
    }

    /**
     * Calculates the distance of two 2 dimensional vectors with the pythagoras
     * algorithm.
     *
     * @param x1 x value from the first vector
     * @param y1 y value from the first vector
     * @param x2 x value from the second vector
     * @param y2 y value from the second vector
     * @return distance
     */
    private double pythagoras(double x1, double y1, double x2, double y2) {
        double oppositeCathetusLenght = (x2 - x1);
        double cathetusLenght = (y2 - y1);

        double oppositeCathetusSquare = Math.pow(oppositeCathetusLenght, 2);
        double cathetusSquare = Math.pow(cathetusLenght, 2);

        return (Math.abs(Math.sqrt(oppositeCathetusSquare + cathetusSquare)));
    }

    /**
     * Calculates the distance between two touches (relativ values)
     *
     * @param firstTouch {@link TouchEvent} from the first touch
     * @param secondTouch {@link TouchEvent} from the second touch
     * @return distance in percent
     */
    private double distance(TouchEvent firstTouch, TouchEvent secondTouch) {
        double x1 = firstTouch.getX();
        double y1 = firstTouch.getY();
        double x2 = secondTouch.getX();
        double y2 = secondTouch.getY();
        return pythagoras(x1, y1, x2, y2);
    }

    /**
     * Action for incomming Pan gesture events
     *
     * @param event incomming pan gesture event
     */
    private void panAction(PanEventImpl event) {
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            int x = (int) (event.getFirstTouch().getX() * component.getDisplaySize().getWidth());
            int y = (int) (event.getFirstTouch().getY() * component.getDisplaySize().getHeight());
            int x2 = (int) (event.getSecondTouch().getX() * component.getDisplaySize().getWidth());
            int y2 = (int) (event.getSecondTouch().getY() * component.getDisplaySize().getHeight());
            if (component.inArea(x, y) && component.inArea(x2, y2)) {
                component.panAction(event);
            }
        }
    }

    /**
     * Action for incomming Zoom gesture events
     *
     * @param event incomming Zoom gesture event
     */
    private void zoomAction(ZoomEventImpl event) {
        if (distance(event.getFirstTouch(), event.getSecondTouch()) > MIN_ZOOM_DISTANCE) {
            for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
                int x = (int) (event.getFirstTouch().getX() * component.getDisplaySize().getWidth());
                int y = (int) (event.getFirstTouch().getY() * component.getDisplaySize().getHeight());
                int x2 = (int) (event.getSecondTouch().getX() * component.getDisplaySize().getWidth());
                int y2 = (int) (event.getSecondTouch().getY() * component.getDisplaySize().getHeight());
                if (component.inArea(x, y) && component.inArea(x2, y2)) {
                    component.zoomAction(event);
                }
            }
        }
    }

    /**
     * Action for incomming Rotation gesture events
     *
     * @param event incomming Rotation gesture event
     */
    private void rotateAction(RotationGestureEventImpl event) {
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            component.rotateAction(event);
        }
    }

    private void dragAction(DragEventImpl event) {
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            int x = (int) (event.getOrigin().getX() * component.getDisplaySize().getWidth());
            int y = (int) (event.getOrigin().getY() * component.getDisplaySize().getHeight());
            int dx = (int) (event.getTranslation().getX() * component.getDisplaySize().getWidth());
            int dy = (int) (event.getTranslation().getY() * component.getDisplaySize().getHeight());
            if (component.inArea(x, y)) {
                if (event.getState().equals(TouchGestureEvent.GestureState.GESTURE_END)) {
                    component.dragEndAction(event.getTouchID(), x, y, dx, dy);
                } else {
                    component.dragAction(event.getTouchID(), x, y, dx, dy);
                }
            }
        }
    }

    @Override
    public void newTouchEvent(TouchEvent event) {
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            int x = (int) (event.getX() * component.getDisplaySize().getWidth());
            int y = (int) (event.getY() * component.getDisplaySize().getHeight());
            if (component.inArea(x, y)) {
                if (event.getTouchState().equals(TouchState.TOUCH_BIRTH)) {
                    component.touchBirthAction(event.getID(), x, y);
                } else if (event.getTouchState().equals(TouchState.TOUCH_LIVING)) {
                    component.touchAliveAction(event.getID(), x, y);
                } else if (event.getTouchState().equals(TouchState.TOUCH_DEAD)) {
                    component.touchDeadAction(event.getID(), x, y);
                }
            }
        }
    }

    @Override
    public void newTouchEvents(List<TouchEvent> events) {
        for (TouchEvent event : events) {
            newTouchEvent(event);
        }
    }

    @Override
    public void newGestureEvent(TouchGestureEvent event) {
        if (event instanceof PanEventImpl) {
            panAction((PanEventImpl) event);
        } else if (event instanceof RotationGestureEventImpl) {
            rotateAction((RotationGestureEventImpl) event);
        } else if (event instanceof ZoomEventImpl) {
            zoomAction((ZoomEventImpl) event);
        } else if (event instanceof DragEventImpl) {
            dragAction((DragEventImpl) event);
        }
    }

    @Override
    public ApplicationType getApplicationType() {
        return ApplicationType.APPLICATION_2D;
    }

    @Override
    public void onGestureEvent(GestureEvent event) {
    }

    @Override
    public void onShapeEvent(ShapeEvent event) {
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            component.onShapeEvent(event);
        }
    }

    @Override
    public void onPenEvent(String device, int x, int y, float force, PenEventDataType penEventState, long timestamp, String eventPageID) {
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            if (component.inArea(x, y)) {
                component.draw(x, y);
            }
        }
    }

    @Override
    public void onHandwritingResult(HandwritingRecognitionEvent event) {
        String topResult = HWRPostProcessing.getResult(event);
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            int x = (int) event.getBoundingBox().getCenterOfGravity().x;
            int y = (int) event.getBoundingBox().getCenterOfGravity().y;
//            if (component.inArea(x, y)) {
            component.hwrAction(topResult);
//            }
        }
    }
}
