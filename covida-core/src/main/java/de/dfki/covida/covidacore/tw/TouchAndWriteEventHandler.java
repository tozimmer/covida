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
import de.dfki.touchandwrite.input.touch.gesture.TouchGestureEvent.GestureState;
import de.dfki.touchandwrite.remote.RemoteTouchAndWriteApplication;
import de.dfki.touchandwrite.remote.event.HandwritingListener;
import de.dfki.touchandwrite.remote.event.TouchEventListener;
import de.dfki.touchandwrite.shape.Shape;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Touch&Write SDK event wrapper
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class TouchAndWriteEventHandler extends RemoteTouchAndWriteApplication implements
        HandwritingListener, TouchEventListener {

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TouchAndWriteEventHandler.class);
    /**
     * {@link TouchAndWriteComponentHandler}
     */
    private TouchAndWriteComponentHandler componentHandler;
    /**
     * {@link IApplication}
     */
    private final IApplication application;
    private Map<Integer, ITouchAndWriteComponent> activeTouchComponents;
    private Map<Integer, ITouchAndWriteComponent> activeGestureComponents;
    private Map<String, ITouchAndWriteComponent> activeDrawComponents;

    /**
     * Creates an instance of the TouchAndWriteEventHandler class wich wraps the
     * touch and pen events from the Touch and Write SDK
     *
     * @param mainFrame UI
     * @param wwPanel Visualization Panel
     */
    public TouchAndWriteEventHandler(IApplication application, TouchAndWriteConfiguration config) {
        super(config);
        this.application = application;
        this.activeTouchComponents = new HashMap<>();
        this.activeGestureComponents = new HashMap<>();
        this.activeDrawComponents = new HashMap<>();
    }

    /**
     * Starts the {@link TouchAndWriteEventHandler}
     */
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
        SortedMap<Integer, ITouchAndWriteComponent> components = new TreeMap<>();
        int id = event.getFirstTouch().getID();
        if (event.getState().equals(GestureState.GESTURE_BEGIN)) {
            for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
                int x = (int) (event.getFirstTouch().getX() * component.getDisplaySize().getWidth());
                int y = (int) (event.getFirstTouch().getY() * component.getDisplaySize().getHeight());
                int x2 = (int) (event.getSecondTouch().getX() * component.getDisplaySize().getWidth());
                int y2 = (int) (event.getSecondTouch().getY() * component.getDisplaySize().getHeight());
                if (component.inArea(x, y) && component.inArea(x2, y2)) {
                    components.put(component.getZPosition(), component);
                }
            }
            if (!components.isEmpty()) {
                activeGestureComponents.put(id,
                        components.get(components.lastKey()));
                components.get(components.lastKey()).panAction(event);
            }
        } else if (event.getState()
                .equals(TouchGestureEvent.GestureState.GESTURE_END)) {
            if (activeTouchComponents.containsKey(id)) {
                ITouchAndWriteComponent component = activeTouchComponents.get(id);
                component.panAction(event);
            }
        } else if (event.getState()
                .equals(TouchGestureEvent.GestureState.GESTURE_UPDATE)) {
            if (activeTouchComponents.containsKey(id)) {
                ITouchAndWriteComponent component = activeTouchComponents.get(id);
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
        SortedMap<Integer, ITouchAndWriteComponent> components = new TreeMap<>();
        int id = event.getFirstTouch().getID();
        if (event.getState().equals(GestureState.GESTURE_BEGIN)) {
            for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
                int x = (int) (event.getFirstTouch().getX()
                        * component.getDisplaySize().getWidth());
                int y = (int) (event.getFirstTouch().getY()
                        * component.getDisplaySize().getHeight());
                int x2 = (int) (event.getSecondTouch().getX()
                        * component.getDisplaySize().getWidth());
                int y2 = (int) (event.getSecondTouch().getY()
                        * component.getDisplaySize().getHeight());
                if (component.inArea(x, y) && component.inArea(x2, y2)) {
                    components.put(component.getZPosition(), component);
                }
            }
            if (!components.isEmpty()) {
                activeGestureComponents.put(id,
                        components.get(components.lastKey()));
                components.get(components.lastKey()).zoomAction(event);
            }
        } else if (event.getState()
                .equals(TouchGestureEvent.GestureState.GESTURE_END)) {
            if (activeTouchComponents.containsKey(id)) {
                ITouchAndWriteComponent component = activeTouchComponents.get(id);
                component.zoomAction(event);
            }
        } else if (event.getState()
                .equals(TouchGestureEvent.GestureState.GESTURE_UPDATE)) {
            if (activeTouchComponents.containsKey(id)) {
                ITouchAndWriteComponent component = activeTouchComponents.get(id);
                component.zoomAction(event);
            }
        }
    }

    /**
     * Action for incomming Rotation gesture events
     *
     * @param event incomming Rotation gesture event
     */
    private void rotateAction(RotationGestureEventImpl event) {
        SortedMap<Integer, ITouchAndWriteComponent> components = new TreeMap<>();
        int id = event.getFirstTouch().getID();
        if (event.getState().equals(GestureState.GESTURE_BEGIN)) {
            for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
                int x = (int) (event.getFirstTouch().getX()
                        * component.getDisplaySize().getWidth());
                int y = (int) (event.getFirstTouch().getY()
                        * component.getDisplaySize().getHeight());
                int x2 = (int) (event.getSecondTouch().getX()
                        * component.getDisplaySize().getWidth());
                int y2 = (int) (event.getSecondTouch().getY()
                        * component.getDisplaySize().getHeight());
                if (component.inArea(x, y) && component.inArea(x2, y2)) {
                    components.put(component.getZPosition(), component);
                }
                if (!components.isEmpty()) {
                    activeGestureComponents.put(id,
                            components.get(components.lastKey()));
                    components.get(components.lastKey()).rotateAction(event);
                }
            }
        } else if (event.getState()
                .equals(TouchGestureEvent.GestureState.GESTURE_UPDATE)) {
            if (activeTouchComponents.containsKey(id)) {
                ITouchAndWriteComponent component = activeTouchComponents.get(id);
                component.rotateAction(event);
            }
        } else if (event.getState()
                .equals(TouchGestureEvent.GestureState.GESTURE_END)) {
            if (activeTouchComponents.containsKey(id)) {
                ITouchAndWriteComponent component = activeTouchComponents.get(id);
                component.rotateAction(event);
            }
        }
    }

    /**
     * Action for incomming drag gesture events
     *
     * @param event incomming drag gesture event
     */
    private void dragAction(DragEventImpl event) {
        SortedMap<Integer, ITouchAndWriteComponent> components = new TreeMap<>();
        int id = event.getTouchID();
        if (event.getState().equals(GestureState.GESTURE_BEGIN)) {
            for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
                int x = (int) (event.getOrigin().getX()
                        * component.getDisplaySize().getWidth());
                int y = (int) (event.getOrigin().getY()
                        * component.getDisplaySize().getHeight());
                if (component.inArea(x, y)) {
                    components.put(component.getZPosition(), component);
                }
            }
            if (!components.isEmpty()) {
                activeGestureComponents.put(id, components.get(components.lastKey()));
                int x = (int) (event.getOrigin().getX()
                        * components.get(components.lastKey()).getDisplaySize().getWidth());
                int y = (int) (event.getOrigin().getY()
                        * components.get(components.lastKey()).getDisplaySize().getHeight());
                int dx = (int) (event.getTranslation().getX()
                        * components.get(components.lastKey()).getDisplaySize().getWidth());
                int dy = (int) (event.getTranslation().getY()
                        * components.get(components.lastKey()).getDisplaySize().getHeight());
                components.get(components.lastKey()).dragAction(id, x, y, dx, dy);
            }
        } else if (event.getState()
                .equals(TouchGestureEvent.GestureState.GESTURE_END)) {
            if (activeTouchComponents.containsKey(event.getTouchID())) {
                ITouchAndWriteComponent component = activeTouchComponents.get(id);
                int x = (int) (event.getOrigin().getX()
                        * component.getDisplaySize().getWidth());
                int y = (int) (event.getOrigin().getY()
                        * component.getDisplaySize().getHeight());
                int dx = (int) (event.getTranslation().getX()
                        * component.getDisplaySize().getWidth());
                int dy = (int) (event.getTranslation().getY()
                        * component.getDisplaySize().getHeight());
                component.dragEndAction(id, x, y, dx, dy);
                activeGestureComponents.remove(id);
            }
        } else if (event.getState()
                .equals(TouchGestureEvent.GestureState.GESTURE_UPDATE)) {
            if (activeTouchComponents.containsKey(event.getTouchID())) {
                ITouchAndWriteComponent component = activeTouchComponents.get(id);
                int x = (int) (event.getOrigin().getX()
                        * component.getDisplaySize().getWidth());
                int y = (int) (event.getOrigin().getY()
                        * component.getDisplaySize().getHeight());
                int dx = (int) (event.getTranslation().getX()
                        * component.getDisplaySize().getWidth());
                int dy = (int) (event.getTranslation().getY()
                        * component.getDisplaySize().getHeight());
                component.dragAction(id, x, y, dx, dy);
            }
        }
    }

    @Override
    public void newTouchEvent(TouchEvent event) {
        int id = event.getID();
        if (event.getTouchState().equals(TouchState.TOUCH_BIRTH)) {
            SortedMap<Integer, ITouchAndWriteComponent> components = new TreeMap<>();
            for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
                int x = (int) (event.getX() * component.getDisplaySize().getWidth());
                int y = (int) (event.getY() * component.getDisplaySize().getHeight());
                if (component.inArea(x, y)) {
                    components.put(component.getZPosition(), component);
                }
            }
            if (!components.isEmpty()) {
                activeTouchComponents.put(event.getID(), components.get(components.lastKey()));
                int x = (int) (event.getX() * components.get(components.lastKey()).getDisplaySize().getWidth());
                int y = (int) (event.getY() * components.get(components.lastKey()).getDisplaySize().getHeight());
                components.get(components.lastKey()).touchBirthAction(id, x, y);
            }
        } else if (event.getTouchState().equals(TouchState.TOUCH_LIVING)) {
            if (activeTouchComponents.containsKey(event.getID())) {
                ITouchAndWriteComponent component = activeTouchComponents.get(event.getID());
                int x = (int) (event.getX() * component.getDisplaySize().getWidth());
                int y = (int) (event.getY() * component.getDisplaySize().getHeight());
                component.touchAliveAction(id, x, y);
            }
        } else if (event.getTouchState().equals(TouchState.TOUCH_DEAD)) {
            if (activeTouchComponents.containsKey(event.getID())) {
                ITouchAndWriteComponent component = activeTouchComponents.get(event.getID());
                int x = (int) (event.getX() * component.getDisplaySize().getWidth());
                int y = (int) (event.getY() * component.getDisplaySize().getHeight());
                component.touchDeadAction(id, x, y);
                activeTouchComponents.remove(id);
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
        SortedMap<Integer, ITouchAndWriteComponent> components = new TreeMap<>();
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            for (Shape shape : event.getDetectedShapes()) {
                for (Point point : shape.getPoints()) {
                    if (component.inArea(point.x, point.y)) {
                        components.put(component.getZPosition(), component);
                    }
                }
            }
        }
        if (!components.isEmpty()) {
            components.get(components.lastKey()).onShapeEvent(event);
        }
    }

    @Override
    public void onPenEvent(String device, int x, int y, float force, PenEventDataType penEventState, long timestamp, String eventPageID) {
        SortedMap<Integer, ITouchAndWriteComponent> components = new TreeMap<>();
        if (penEventState.equals(PenEventDataType.NEW_SESSION)) {
            for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
                if (component.inArea(x, y)) {
                    components.put(component.getZPosition(), component);
                }
            }
            if (!components.isEmpty()) {
                activeDrawComponents.put(device, components.get(components.lastKey()));
            }
        } else if (penEventState.equals(PenEventDataType.PEN_UP)) {
            if (activeDrawComponents.containsKey(device)) {
                activeDrawComponents.get(device).draw(x, y);
                activeDrawComponents.remove(device);
            }
        } else {
            if (activeDrawComponents.containsKey(device)) {
                activeDrawComponents.get(device).draw(x, y);
            }
        }
    }

    @Override
    public void onHandwritingResult(HandwritingRecognitionEvent event) {
        SortedMap<Integer, ITouchAndWriteComponent> components = new TreeMap<>();
        String topResult = HWRPostProcessing.getResult(event);
        for (ITouchAndWriteComponent component : componentHandler.getComponents()) {
            int x = (int) event.getBoundingBox().getCenterOfGravity().x;
            int y = (int) event.getBoundingBox().getCenterOfGravity().y;
            if (component.inArea(x, y)) {
                components.put(component.getZPosition(), component);
            }
        }
        if (!components.isEmpty()) {
            components.get(components.lastKey()).hwrAction(topResult);
        }
    }
}
