/*
 * LockState.java
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
package de.dfki.covida.ui.components;

import com.jme.math.Vector2f;
import com.jme.scene.Spatial;
import de.dfki.covida.ui.CovidaBoard;
import de.dfki.covida.ui.components.annotation.AnnotationSearch;
import de.dfki.covida.ui.components.video.VideoComponent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.DragEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.RotationGestureEvent;
import de.dfki.touchandwrite.analyser.touch.gestures.events.ZoomEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * 
 * 
 * 
 * @author Tobias Zimmermann
 * 
 */
public class LockState {

    private static LockState instance;
    private Map<Integer, Integer> touchLocks;
    protected HashMap<Integer, List<Spatial>> nodeOrders;
    private ArrayList<VideoComponent> videos;
    private ArrayList<Object> components;
    private Logger log = Logger.getLogger(LockState.class);

    /**
     * 
     */
    private LockState() {
        touchLocks = new HashMap<Integer, Integer>();
        nodeOrders = new HashMap<Integer, List<Spatial>>();
        videos = new ArrayList<VideoComponent>();
        components = new ArrayList<Object>();
    }

    /**
     * 
     * @param component
     * @return component id
     */
    public synchronized int registerComponent(Object component) {
        components.add(component);
        return (components.size() - 1);
    }

    public boolean inArea(int x, int y) {
        for (Object component : components) {
            if (component instanceof CovidaComponent) {
                if (((CovidaComponent) component).inArea(x, y)) {
                    return true;
                }

            }
        }
        return false;
    }

    public static LockState getInstance() {
        if (instance == null) {
            instance = new LockState();
        }
        return instance;
    }

    public void addvideo(VideoComponent video) {
        if (!videos.contains(video)) {
            videos.add(video);
        }
    }

    public boolean onTop(int id, Vector2f pos, CovidaBoard board) {
        if (isTouchLocked(id)) {
            return (getTouchLock(id) == board.getID());
        }
        Boolean onTop = !inArea((int) pos.x, (int) pos.y);
        if (onTop){
            setTouchLock(id, board.getID());
        }
        return onTop;
    }
    
    /**
     * OnTop detection
     * 
     * @param object
     *            (Vector2f / ShapeEvent)
     * @param CovidaComponent
     * @return true if component is on top
     */
    public boolean onTop(int id, Object object, CovidaComponent coVidAComponent) {
        if (isTouchLocked(id)) {
            return (getTouchLock(id) == coVidAComponent.getId());
        }
        Boolean onTop = coVidAComponent.inArea(object);
        if (coVidAComponent.isAlwaysOnTop()) {
            if (coVidAComponent instanceof AnnotationSearch) {
                if (((AnnotationSearch) coVidAComponent).isOpen()) {
                    return onTop;
                } else {
                    return false;
                }
            } else {
                return onTop;
            }
        }
        for (Object component : components) {
            if (component instanceof CovidaComponent) {
                if (((CovidaComponent) component).isAlwaysOnTop()
                        && ((CovidaComponent) component).inArea(object)) {
                    if (component instanceof AnnotationSearch) {
                        if (((AnnotationSearch) component).isOpen()) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            if (component instanceof CovidaComponent) {
                if (((CovidaComponent) component).inArea(object)
                        && !((CovidaComponent) component).isAlwaysOnTop()
                        && !(component instanceof TextOverlay)) {
                    if (((CovidaComponent) component).getNodeIndex() > coVidAComponent.getNodeIndex()) {
                        return false;
                    }
                }
            }
        }
        if (onTop) {
            setTouchLock(id, coVidAComponent.getId());
            coVidAComponent.toFront();
        }
        return onTop;
    }

    /**
     * 
     * @param touchId
     */
    public void removeTouchLock(int touchId) {
        if (isTouchLocked(touchId)) {
//            log.debug("Remove touch lock: " + touchId);
            for (Object component : components) {
                if (component instanceof CovidaComponent) {
                    CovidaComponent cc = (CovidaComponent) component;
                    if (cc.getId() == touchLocks.get(new Integer(touchId))) {
//                        log.debug("Touch dead action for " + cc.getName());
                        cc.touchDeadAction(touchId);
                    }
                }
            }
            touchLocks.remove(new Integer(touchId));
        }
    }

    /**
     * 
     * @param touchId
     */
    public void removeTouchLockWithoutAction(int touchId) {
        if (isTouchLocked(touchId)) {
//            log.debug("Remove touch lock without action: " + touchId);
            touchLocks.remove(new Integer(touchId));
        }
    }

    /**
     * 
     * @param touchId
     * @return
     */
    public boolean isTouchLocked(int touchId) {
//		log.debug("touchID " + touchId + " is locked = "
//				+ touchLocks.containsKey(touchId));
        return touchLocks.containsKey(touchId);
    }

    /**
     * 
     * @param touchId
     * @return
     */
    public int getTouchLock(int touchId) {
        if (isTouchLocked(touchId)) {
            return touchLocks.get(new Integer(touchId));
        } else {
            return Integer.MAX_VALUE;
        }
    }
    
    private void setTouchLock(int touchId, int id) {
        if(touchId == -1){
            return;
        }
        touchLocks.put(touchId, id);
    }

    public void forceTouchLock(int touchId, int id) {
        setTouchLock(touchId, id);
    }

    public boolean onTop(ZoomEvent e, CovidaComponent coVidAComponent) {
        if (!onTop(e.getFirstTouch().getID(),
                new Vector2f((float) e.getFirstTouch().getX(), (float) e.getFirstTouch().getY()),
                coVidAComponent)) {
            return false;
        }
        if (!onTop(e.getSecondTouch().getID(),
                new Vector2f((float) e.getSecondTouch().getX(), (float) e.getSecondTouch().getY()),
                coVidAComponent)) {
            return false;
        }
        return true;
    }

    public boolean onTop(DragEvent e, CovidaComponent coVidAComponent) {
//        log.debug("Drag onTop: " + onTop(e.getTouchID(),
//                new Vector2f((float) e.getOrigin().getX(), (float) e.getOrigin().getY()),
//                coVidAComponent));
        return onTop(e.getTouchID(),new Vector2f((float) e.getOrigin().getX(), (float) e.getOrigin().getY()),coVidAComponent);
    }

    public boolean onTop(RotationGestureEvent e,
            CovidaComponent coVidAComponent) {
        if (!onTop(e.getFirstTouch().getID(),
                new Vector2f((float) e.getFirstTouch().getX(), (float) e.getFirstTouch().getY()),
                coVidAComponent)) {
            return false;
        }
        if (!onTop(e.getSecondTouch().getID(),
                new Vector2f((float) e.getSecondTouch().getX(), (float) e.getSecondTouch().getY()),
                coVidAComponent)) {
            return false;
        }
        return true;
    }
}
