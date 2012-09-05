/*
 * AnnotationSearch.java
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
package de.dfki.covida.ui.components.annotation;

import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.angelfont.BitmapFont.Align;
import de.dfki.covida.data.VideoAnnotationData;
import de.dfki.covida.ui.components.TextOverlay;
import de.dfki.covida.ui.components.video.VideoComponent;
import de.dfki.touchandwrite.action.DrawAction;
import de.dfki.touchandwrite.action.HWRAction;
import de.dfki.touchandwrite.action.TouchAction;
import de.dfki.touchandwrite.action.TouchActionEvent;
import de.dfki.touchandwrite.input.pen.event.ShapeEvent;
import de.dfki.touchandwrite.input.pen.hwr.HWRResultSet;
import de.dfki.touchandwrite.input.pen.hwr.HandwritingRecognitionEvent;
import de.dfki.touchandwrite.math.FastMath;
import de.dfki.touchandwrite.visual.components.ComponentType;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * Component which displays annotation data of VideoComponent.
 *
 * @author Tobias Zimmermann
 *
 */
public class AnnotationSearch extends Field {

    protected ArrayList<VideoComponent> videos;

    /**
     * Search field constructor
     *
     * @param resource
     * @param video
     * @param listField
     * @param id
     * @param node
     * @param width
     * @param height
     */
    public AnnotationSearch(String resource, Node node, int width, int height, ArrayList<VideoComponent> videos) {
        super(ComponentType.COMPONENT_2D, "AnnotationSearch", node);
        this.width = width;
        this.height = height;
        this.image = resource;
        this.videos = videos;
        hwrResults = new ArrayList<String>();
        hwrEvents = new ArrayList<HandwritingRecognitionEvent>();
        mapping = new HashMap<Integer, Integer>();
        entriesMapping = new ArrayList<Map<Integer, Integer>>();
        data = new ArrayList<VideoAnnotationData>();
        entryMap = new HashMap<Integer, ArrayList<TextOverlay>>();
        titles = new ArrayList<TextOverlay>();
        hwr = new ArrayList<TextOverlay>();
        result = new HashMap<Integer, ArrayList<Integer>>();
        resultString = new HashMap<Integer, ArrayList<String>>();
    }

    protected void initTextures() {
        // ---- Background Texture state initialization ----
        ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        ts.setCorrectionType(TextureState.CorrectionType.Perspective);
        ts.setEnabled(true);
        texture = TextureManager.loadTexture(getClass().getClassLoader().getResource(image));
        ts.setTexture(texture);
        quad = new Quad("Display image quad", width, height);
        quad.setRenderState(ts);
        quad.setRenderState(this.initalizeBlendState());
        quad.updateRenderState();
        getNode().attachChild(quad);
        // Spacer
        tsSpacer = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
        tsSpacer.setCorrectionType(TextureState.CorrectionType.Perspective);
        tsSpacer.setEnabled(true);
        textureSpacer = TextureManager.loadTexture(getClass().getClassLoader().getResource("media/textures/info_spacer.png"));
        tsSpacer.setTexture(textureSpacer);
    }

    @Override
    public void handwritingResult(HandwritingRecognitionEvent event) {
        // TODO pen id!
//        log.debug("HWR Event: " + event.toString());
        if (!isOpen()) {
            return;
        }
        if (getLockState().onTop(
                -1,
                new Vector2f(event.getBoundingBox().getCenterOfGravity().x,
                (getDisplay().y - event.getBoundingBox().getCenterOfGravity().y)), this)) {
            this.hwrEvents.add(event);
            int size = event.getHWRResultSet().getWords().size();
            hwrResults = new ArrayList<String>();
            for (int i = 0; i < size; i++) {
                this.hwrResults.add(event.getHWRResultSet().getWords().get(i).getCandidates().peek().getRecogntionResult());
            }
            update();
        }
    }

    @Override
    protected void touchAliveAction(TouchActionEvent e) {
        setActiveTitle(getSelectedTitle(e.getX(), e.getY()));
        setActiveEntry(getSelectedEntry(e.getX(), e.getY()));
        Vector3f nodePosition = getRootNode().getLocalTranslation();
        if (!(lastTouch == null)) {
            if (nodePosition.y > getDisplay().y / 2.f) {
                float diffY = (nodePosition.y - lastTouch.get(e.getID()).y) - (nodePosition.y - e.getY());
                yDrag = yDrag + diffY;
            } else {
                float diffY = (nodePosition.y - lastTouch.get(e.getID()).y) - (nodePosition.y - e.getY());
                yDrag = yDrag - diffY;
            }
            if (nodePosition.x > getDisplay().x / 2.f) {
                float diffX = (nodePosition.x - lastTouch.get(e.getID()).x) - (nodePosition.x - e.getX());
                xDrag = xDrag + diffX;
            } else {
                float diffX = (nodePosition.x - lastTouch.get(e.getID()).x) - (nodePosition.x - e.getX());
                xDrag = xDrag - diffX;
            }
        }
        lastTouch.put(e.getID(),
                new Vector2f(e.getX(), e.getY()));
    }

    @Override
    protected void touchDeadAction(TouchActionEvent e) {
        lastTouch.remove(e.getID());
        if (yDrag > getDisplay().y / 30.f
                && xDrag > getDisplay().x / 30.f) {
            this.close();
        }
        yDrag = 0;
        setSelectedTitle(getSelectedTitle(e.getX(), e.getY()));
        setSelectedEntry(getSelectedEntry(e.getX(), e.getY()));
    }

    @Override
    protected void touchBirthAction(TouchActionEvent e) {
//        TouchState state = e.getTouchState();
//        if (state == TouchState.TOUCH_BIRTH || state == TouchState.TOUCH_LIVING) {
//            if (inArea(e.getX(), e.getY())) {
//                getLockState().forceTouchLock(e.getID(), getId());
//                lastTouch.put(e.getID(), new Vector2f(e.getX(), e.getY()));
//            }
//        }
//        if (getLockState().isTouchLocked(e.getID())) {
//            if (getLockState().getTouchLock(e.getID()) == getId()) {
//                if (state == TouchState.TOUCH_BIRTH || state == TouchState.TOUCH_LIVING) {
//                } else if (state == TouchState.TOUCH_DEAD) {
//                }
//            }
//        }
        touchAliveAction(e);
    }

    @Override
    public void clearHwrResults() {
        hwrResults.clear();
    }

    /**
     *
     * @param x
     * @param y
     * @return
     * <code>Integer</code> , entryId or -1 if there is no entry on x,y
     */
    public int getSelectedEntry(int x, int y) {
        if (entryMap.containsKey(mapping.get(selectedTitle))) {
            for (int i = 0; i < entryMap.get(mapping.get(selectedTitle)).size(); i++) {
                if (entryMap.get(mapping.get(selectedTitle)).get(i).inArea(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     * <code>Integer</code> , tilteId or -1 if there is no title on x,y
     */
    public int getSelectedTitle(int x, int y) {
        for (int i = 0; i < titles.size(); i++) {
            if (titles.get(i).inArea(x, y)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the active entry on the ListField
     *
     * @param index
     */
    public void setActiveEntry(int index) {
        if (entryMap.get(mapping.get(selectedTitle)) != null && index > -1
                && index < entryMap.get(mapping.get(selectedTitle)).size()) {
            for (TextOverlay entry : entryMap.get(mapping.get(selectedTitle))) {
                entry.setColor(defaultColor);
            }
            entryMap.get(mapping.get(selectedTitle)).get(index).setColor(activeColor);
            entryMap.get(mapping.get(selectedTitle)).get(index).scaleAnimation(2.f, 2.f);
        }
    }

    /**
     * Sets the selected entry on the ListField
     *
     * @param index
     */
    public void setSelectedEntry(int index) {
        if (index > -1
                && index < entryMap.get(mapping.get(selectedTitle)).size() + 1) {
            for (TextOverlay entry : entryMap.get(mapping.get(selectedTitle))) {
                entry.setColor(defaultColor);
            }
            if (entryMap.size() > selectedTitle) {
                if (entryMap.get(mapping.get(selectedTitle)).size() > index) {
                    entryMap.get(mapping.get(selectedTitle)).get(index).setColor(selectedColor);
                    videos.get(mapping.get(selectedTitle)).loadAnnotationData(
                            entriesMapping.get(selectedTitle).get(index));
                    videos.get(mapping.get(selectedTitle)).pause();
                    videos.get(mapping.get(selectedTitle)).toFront();
                } else {
                    log.debug("entries.get(" + selectedTitle + ").size()<"
                            + index);
                }
            } else {
                log.debug("entries.size()<=" + selectedTitle);
            }

        }
    }

    /**
     * Sets the active entry on the ListField
     *
     * @param titleID
     */
    public void setActiveTitle(int titleID) {
        if (titleID > -1 && titleID < titles.size()) {
            for (TextOverlay title : titles) {
                title.setColor(defaultColor);
            }
            titles.get(titleID).setColor(activeColor);
        }
    }

    @Override
    protected void update() {
        if (this.hwrResults != null) {
            // TODO handle to much entries (display capacity)
            for (TextOverlay to : hwr) {
                to.detach();
            }
            for (TextOverlay to : titles) {
                to.detach();
            }
            if (entryMap.containsKey(mapping.get(selectedTitle))) {
                for (TextOverlay entry : entryMap.get(mapping.get(selectedTitle))) {
                    // TODO detach!
                    entry.fadeOut(1.f);
                }
            }
            int x = (int) (-width / 2.5f);
            mapping = new HashMap<Integer, Integer>();
            entriesMapping = new ArrayList<Map<Integer, Integer>>();
            entryMap = new HashMap<Integer, ArrayList<TextOverlay>>();
            titles = new ArrayList<TextOverlay>();
            hwr = new ArrayList<TextOverlay>();
            result = new HashMap<Integer, ArrayList<Integer>>();
            resultString = new HashMap<Integer, ArrayList<String>>();
            for (int i = 0; i < hwrResults.size(); i++) {
                Node node = new Node("HWR Search Text Node");
                node.setLocalTranslation(x, getTextY(2 + i), 0);
                getNode().attachChild(node);
                hwr.add(new TextOverlay(node, this));
                hwr.get(i).setText(hwrResults.get(i));
                hwr.get(i).setSize(FONT_SIZE);
                hwr.get(i).setFont(1);
                hwr.get(i).setColor(new ColorRGBA(0.75f, 0.75f, 0.75f, 0));
                hwr.get(i).fadeIn((float) i * 1.f + 1.f);
            }
        }
        // TODO max limit for list
        int x;
        data.clear();
        result = new HashMap<Integer, ArrayList<Integer>>();
        for (VideoComponent video : videos) {
            data.add(video.getAnnotationData());
            log.debug("retrieved data: " + data);
        }
        for (TextOverlay title : titles) {
            title.detach();
        }
        titles = new ArrayList<TextOverlay>();
        search();
        log.debug("search results: " + result);
        int index = 0;
        for (int i = 0; i < data.size(); i++) {
            Map<Integer, Integer> entriesMap = new HashMap<Integer, Integer>();
            if (result.containsKey(new Integer(i))) {
                // display video title
                String title = data.get(i).title;
                log.debug("draw title: " + title);
                x = (int) (-quad.getWidth() / 4.15f);
                Node node = new Node("AnnotationSearch title node");
                node.setLocalTranslation(x, getTextY(index + 2), 0);
                getNode().attachChild(node);
                titles.add(new TextOverlay(node, this));
                titles.get(titles.size() - 1).setFont(1);
                titles.get(titles.size() - 1).setSize(FONT_SIZE);
                titles.get(titles.size() - 1).setAlign(Align.Left);
                titles.get(titles.size() - 1).setText(title);
                // get search results (entries)
                ArrayList<TextOverlay> resultEntries = new ArrayList<TextOverlay>();
                for (int j = 0; j < result.get(new Integer(i)).size(); j++) {
                    x = (int) (quad.getWidth() / 7.85f);
                    String entry = videos.get(i).getTimeCode(
                            data.get(i).annotations.get(result.get(
                            new Integer(i)).get(j)).time_start)
                            + " - " + resultString.get(new Integer(i)).get(j);
                    log.debug("draw entry: " + entry);
                    Node n = new Node("AnnotationSearch entry node");
                    n.setLocalTranslation(x, getTextY(j + 2), 0);
                    resultEntries.add(new TextOverlay(n, this));
                    resultEntries.get(j).setText(entry);
                    resultEntries.get(j).setFont(1);
                    resultEntries.get(j).setSize(FONT_SIZE);
                    resultEntries.get(j).setAlign(Align.Left);
                    resultEntries.get(j).fadeIn((float) j * 1.f + 1.f);
                    entriesMap.put(resultEntries.size() - 1,
                            result.get(new Integer(i)).get(j));
                }
                entryMap.put(i, resultEntries);
                mapping.put(index, i);
                entriesMapping.add(index, entriesMap);
                index++;
            }
        }
        if (titles.size() > index) {
            for (int i = index; i < titles.size(); i++) {
                titles.remove(i);
            }
        }
        if (titles.size() > 0) {
            setSelectedTitle(0);
        }
    }

    /**
     * Sets the selected title on the SearchField
     *
     * @param index
     */
    public void setSelectedTitle(int index) {
        if (index > -1 && index < titles.size() + 1) {
            if (entryMap.containsKey(mapping.get(index))) {
                for (TextOverlay title : titles) {
                    title.setColor(defaultColor);
                }
                if (entryMap.containsKey(mapping.get(selectedTitle))) {
                    for (TextOverlay entry : entryMap.get(mapping.get(selectedTitle))) {
                        // TODO detach!
                        entry.fadeOut(1.f);
                    }
                }
                titles.get(index).setColor(selectedColor);
                selectedTitle = index;
                for (TextOverlay entry : entryMap.get(mapping.get(index))) {
                    entry.attach(getNode());
                    entry.fadeIn(1.5f);
                }
            } else {
                log.debug("!(entryMap.containsKey(index)) index: " + index
                        + ")");
            }
        }
    }

    protected float getTextY(int position) {
        return textBeginY - TEXT_SPACER - FONT_SIZE * (position)
                - (float) FONT_SIZE / 2.f;
    }

    /**
     * Checks the hwr result and chooses the best result.
     *
     * @param hwrResultSet
     * @return
     */
    protected String checkHWRResult(HWRResultSet hwrResultSet) {
        hwrResultSet.getWords();
        return hwrResultSet.topResult();
    }

    /**
     *
     * @param x
     * @param y
     * @param angle - angle in degree
     * @param width
     * @param height
     */
    protected void addSpacer(int x, int y, float angle, int width, int height) {
        Quaternion q = new Quaternion();
        q = q.fromAngleAxis(FastMath.DEG_TO_RAD * angle, new Vector3f(0, 0, 1));
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        spacerQuad.setLocalRotation(q);
        getNode().attachChild(spacerQuad);
    }

    @Override
    public void draw(ShapeEvent shape) {
        /**
         * do nothing
         */
    }

    @Override
    public void setCurrentPenColor(Color color) {
        this.currentPenColor = color;
    }

    @Override
    public void activatePenPressure() {
        this.penPressure = true;
    }

    @Override
    public void deactivatePenPressure() {
        this.penPressure = false;

    }

    @Override
    public boolean isPenPressureActivated() {
        return penPressure;
    }

    @Override
    public float getPenThickness() {
        return penThickness;
    }

    @Override
    public void setPenThickness(float thickness) {
        this.penThickness = thickness;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.dfki.touchandwrite.visual.components.TouchAndWriteComponent#initComponent
     * ()
     */
    @Override
    public void initComponent() {
        super.initComponent();
        super.setAlwaysOnTop(true);
        super.setRootNode(getNode().getParent());
        getNode().setLocalScale(new Vector3f(1, 1, 1));
        initTextures();
        textBeginY = (int) (quad.getHeight() / 2.0f);
        this.drawAction = new DrawAction(this);
        this.touchAction = new TouchAction(this);
        this.hwrAction = new HWRAction(this);
        int x = (int) (0);
        Node node = new Node("AnnotationSearch Entry Node");
        getNode().attachChild(node);
        node.setLocalTranslation(x, getTextY(0) - FONT_SIZE / 4.f, 0);
        TextOverlay caption = new TextOverlay(node, this);
        caption.setSize((int) (FONT_SIZE * 1.5f));
        caption.setText("Write here for annotation search:");
        caption.setFont(2);
        addSpacer(x, (int) (getTextY(0) - FONT_SIZE), 0,
                (int) (quad.getWidth() / 1.1f), TEXT_SPACER);
        x = (int) (getWidth() / 9.f);
        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
        x = (int) -(getWidth() / 4.f);
        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
    }

    private void search() {
        result = new HashMap<Integer, ArrayList<Integer>>();
        resultString = new HashMap<Integer, ArrayList<String>>();
        if (data != null && hwrResults != null) {
            /**
             * video index
             */
            int i;
            /**
             * annotation index
             */
            int j;
            for (String hwrResult : hwrResults) {
                // exact search
                for (i = 0; i < data.size(); i++) {
                    if (data.get(i) != null) {
                        for (j = 0; j < data.get(i).size(); j++) {
                            if (data.get(i).annotations.get(j).description != null) {
                                for (String s : data.get(i).annotations.get(j).description.split(" ")) {
                                    if (s.equals(hwrResult)) {
                                        if (result.containsKey(new Integer(i))) {
                                            result.get(new Integer(i)).add(j);
                                            resultString.get(new Integer(i)).add(s);
                                        } else {
                                            ArrayList<Integer> list = new ArrayList<Integer>();
                                            ArrayList<String> stringList = new ArrayList<String>();
                                            list.add(j);
                                            stringList.add(s);
                                            result.put(new Integer(i), list);
                                            resultString.put(new Integer(i),
                                                    stringList);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                // case insensitive search
                for (i = 0; i < data.size(); i++) {
                    if (data.get(i) != null) {
                        for (j = 0; j < data.get(i).size(); j++) {
                            if (data.get(i).annotations.get(j).description != null) {
                                for (String s : data.get(i).annotations.get(j).description.split(" ")) {
                                    if (s.equalsIgnoreCase(hwrResult)) {
                                        if (result.containsKey(new Integer(i))) {
                                            if (!result.get(new Integer(i)).contains(new Integer(j))) {
                                                result.get(new Integer(i)).add(
                                                        j);
                                                resultString.get(new Integer(i)).add(s);
                                            } else {
                                            }
                                        } else {
                                            ArrayList<Integer> list = new ArrayList<Integer>();
                                            ArrayList<String> stringList = new ArrayList<String>();
                                            list.add(j);
                                            stringList.add(s);
                                            result.put(new Integer(i), list);
                                            resultString.put(new Integer(i),
                                                    stringList);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                // wrap around search
                for (i = 0; i < data.size(); i++) {
                    if (data.get(i) != null) {
                        for (j = 0; j < data.get(i).size(); j++) {
                            if (data.get(i).annotations.get(j).description != null) {
                                for (String s : data.get(i).annotations.get(j).description.split(" ")) {
                                    if (s.contains(hwrResult)) {
                                        if (result.containsKey(new Integer(i))) {
                                            if (!result.get(new Integer(i)).contains(new Integer(j))) {
                                                result.get(new Integer(i)).add(
                                                        j);
                                                resultString.get(new Integer(i)).add(s);
                                            } else {
                                            }
                                        } else {
                                            ArrayList<Integer> list = new ArrayList<Integer>();
                                            ArrayList<String> stringList = new ArrayList<String>();
                                            list.add(j);
                                            stringList.add(s);
                                            result.put(new Integer(i), list);
                                            resultString.put(new Integer(i),
                                                    stringList);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                // Levenshtein-Distance
                for (i = 0; i < data.size(); i++) {
                    if (data.get(i) != null) {
                        for (j = 0; j < data.get(i).size(); j++) {
                            if (data.get(i).annotations.get(j).description != null) {
                                for (String s : data.get(i).annotations.get(j).description.split(" ")) {
                                    int distance = StringUtils.getLevenshteinDistance(hwrResult, s);
                                    log.debug("SearchString: " + hwrResults
                                            + " - " + s + " distance: "
                                            + distance);
                                    if (distance < 3) {
                                        if (result.containsKey(new Integer(i))) {
                                            if (!result.get(new Integer(i)).contains(new Integer(j))) {
                                                result.get(new Integer(i)).add(
                                                        j);
                                                resultString.get(new Integer(i)).add(s);
                                            } else {
                                            }
                                        } else {
                                            ArrayList<Integer> list = new ArrayList<Integer>();
                                            ArrayList<String> stringList = new ArrayList<String>();
                                            list.add(j);
                                            stringList.add(s);
                                            result.put(new Integer(i), list);
                                            resultString.put(new Integer(i),
                                                    stringList);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}