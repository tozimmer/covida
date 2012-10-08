/*
 * AnnotationSearchField.java
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
package de.dfki.covida.visualjme2.components.annotation;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Quad;
import com.jme.util.GameTaskQueueManager;
import com.jmex.angelfont.BitmapFont.Align;
import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationData;
import de.dfki.covida.covidacore.data.AnnotationStorage;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.covidacore.utils.AnnotationSearch;
import de.dfki.covida.covidacore.utils.VideoUtils;
import de.dfki.covida.visualjme2.animations.CloseAnimation;
import de.dfki.covida.visualjme2.animations.OpenAnimation;
import de.dfki.covida.visualjme2.components.CovidaFieldComponent;
import de.dfki.covida.visualjme2.components.CovidaTextComponent;
import de.dfki.covida.visualjme2.utils.AddControllerCallable;
import de.dfki.covida.visualjme2.utils.AttachChildCallable;
import de.dfki.covida.visualjme2.utils.JMEUtils;
import de.dfki.covida.visualjme2.utils.RemoveControllerCallable;
import de.dfki.touchandwrite.math.FastMath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component which displays annotation dataList of VideoComponent.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class AnnotationSearchField extends CovidaFieldComponent implements
        IControlableComponent {

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
    public AnnotationSearchField(String resource, int width, int height) {
        super("AnnotationSearch");
        this.width = width;
        this.height = height;
        this.image = resource;
        hwrResults = new ArrayList<>();
        mapping = new HashMap<>();
        entriesMapping = new ArrayList<>();
        entryMap = new HashMap<>();
        titles = new ArrayList<>();
        hwr = new ArrayList<>();
        super.setAlwaysOnTop(true);
        setLocalScale(new Vector3f(1, 1, 1));
        initTextures();
        textBeginY = (int) (quad.getHeight() / 2.0f);
        int x = (int) (0);
        CovidaTextComponent caption = new CovidaTextComponent(this);
        caption.setLocalTranslation(x, getTextY(0) - FONT_SIZE / 4.f, 0);
        caption.setSize((int) (FONT_SIZE * 1.5f));
        caption.setText("Write here for annotation search:");
        caption.setFont(2);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, caption.node));
        addSpacer(x, (int) (getTextY(0) - FONT_SIZE), 0,
                (int) (quad.getWidth() / 1.1f), TEXT_SPACER);
        x = (int) (getWidth() / 9.f);
        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
        x = (int) -(getWidth() / 4.f);
        addSpacer(x, 0, 90, (int) (quad.getHeight() / 1.1f), TEXT_SPACER);
    }

    @Override
    public void clearHwrResults() {
        hwrResults.clear();
    }

    /**
     *
     * @param x
     * @param y
     * @return <code>Integer</code> , entryId or -1 if there is no entry on x,y
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
     * @return <code>Integer</code> , tilteId or -1 if there is no title on x,y
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
            for (CovidaTextComponent entry : entryMap.get(mapping.get(selectedTitle))) {
                entry.setColor(defaultColor);
            }
            entryMap.get(mapping.get(selectedTitle)).get(index).setColor(activeColor);
            entryMap.get(mapping.get(selectedTitle)).get(index).startScaleAnimation();
        }
    }

    /**
     * Sets the selected entry on the ListField
     *
     * @param index
     */
    public void setSelectedEntry(int index) {
//        if (index > -1
//                && index < entryMap.get(mapping.get(selectedTitle)).size() + 1) {
//            for (CovidaTextComponent entry : entryMap.get(mapping.get(selectedTitle))) {
//                entry.setColor(defaultColor);
//            }
//            
//            if (entryMap.size() > selectedTitle) {
//                if (entryMap.get(mapping.get(selectedTitle)).size() > index) {
//                    entryMap.get(mapping.get(selectedTitle)).get(index).setColor(selectedColor);
//                    AnnotationStorage.getInstance().loadAnnotation(entriesMapping.get(index).get(log));
//                } else {
//                    log.debug("entries.get(" + selectedTitle + ").size()<"
//                            + index);
//                }
//            } else {
//                log.debug("entries.size()<=" + selectedTitle);
//            }
//
//        }
    }

    /**
     * Sets the active entry on the ListField
     *
     * @param titleID
     */
    public void setActiveTitle(int titleID) {
        if (titleID > -1 && titleID < titles.size()) {
            for (CovidaTextComponent title : titles) {
                title.setColor(defaultColor);
            }
            titles.get(titleID).setColor(activeColor);
        }
    }

    @Override
    protected void update() {
        if (this.hwrResults != null) {
            // TODO handle to much entries (display capacity)
            for (CovidaTextComponent to : hwr) {
                to.detach();
            }
            for (CovidaTextComponent to : titles) {
                to.detach();
            }
            if (entryMap.containsKey(mapping.get(selectedTitle))) {
                for (CovidaTextComponent entry : entryMap.get(mapping.get(selectedTitle))) {
                    // TODO detach!
                    entry.fadeOut(1.f);
                }
            }
            int x = (int) (-width / 2.5f);
            mapping = new HashMap<>();
            entriesMapping = new ArrayList<>();
            entryMap = new HashMap<>();
            titles = new ArrayList<>();
            hwr = new ArrayList<>();
            for (int i = 0; i < hwrResults.size(); i++) {
                CovidaTextComponent hwrText = new CovidaTextComponent(this);
                hwrText.setLocalTranslation(x, getTextY(2 + i), 0);
                GameTaskQueueManager.getManager().update(new AttachChildCallable(node, hwrText.node));
                hwr.add(hwrText);
                hwr.get(i).setText(hwrResults.get(i));
                hwr.get(i).setSize(FONT_SIZE);
                hwr.get(i).setFont(1);
                hwr.get(i).setColor(new ColorRGBA(0.75f, 0.75f, 0.75f, 0));
                hwr.get(i).fadeIn((float) i * 1.f + 1.f);
            }
        }
        // TODO max limit for list
        int x;

        for (CovidaTextComponent title : titles) {
            title.detach();
        }
        titles = new ArrayList<>();
        Map<AnnotationData, List<Annotation>> searchResult = AnnotationSearch
                .search(hwrResults, AnnotationStorage.getInstance().getAnnotationDatas());
        log.debug("search results: " + searchResult.toString());
        int index = 0;
        for (AnnotationData data : AnnotationStorage.getInstance().getAnnotationDatas()) {
            Map<Integer, Annotation> entriesMap = new HashMap<>();
            if (searchResult.containsKey(data)) {
                // display video title
                String title = data.title;
                log.debug("draw title: " + title);
                x = (int) (-quad.getWidth() / 4.15f);
                CovidaTextComponent titleText = new CovidaTextComponent(this);
                titleText.setLocalTranslation(x, getTextY(index + 2), 0);
                GameTaskQueueManager.getManager().update(new AttachChildCallable(node, titleText.node));
                titles.add(titleText);
                titles.get(titles.size() - 1).setFont(1);
                titles.get(titles.size() - 1).setSize(FONT_SIZE);
                titles.get(titles.size() - 1).setAlign(Align.Left);
                titles.get(titles.size() - 1).setText(title);
                // get search results (entries)
                ArrayList<CovidaTextComponent> resultEntries = new ArrayList<>();
                for (Annotation annotation : searchResult.get(data)) {
                    x = (int) (quad.getWidth() / 7.85f);
                    String entry = VideoUtils.getTimeCode(annotation.time_start);
                    log.debug("draw entry: " + entry);
                    CovidaTextComponent textOverlay = new CovidaTextComponent(this);
                    textOverlay.setLocalTranslation(x, getTextY(resultEntries.size() + 2), 0);
                    textOverlay.setText(entry);
                    textOverlay.setFont(1);
                    textOverlay.setSize(FONT_SIZE);
                    textOverlay.setAlign(Align.Left);
                    textOverlay.fadeIn((float) resultEntries.size() * 1.f + 1.f);
                    resultEntries.add(textOverlay);

                    entriesMap.put(resultEntries.size() - 1, annotation);
                }
                entryMap.put(index, resultEntries);
                mapping.put(index, data);
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
                for (CovidaTextComponent title : titles) {
                    title.setColor(defaultColor);
                }
                if (entryMap.containsKey(mapping.get(selectedTitle))) {
                    for (CovidaTextComponent entry : entryMap.get(mapping.get(selectedTitle))) {
                        // TODO detach!
                        entry.fadeOut(1.f);
                    }
                }
                titles.get(index).setColor(selectedColor);
                selectedTitle = index;
                for (CovidaTextComponent entry : entryMap.get(mapping.get(index))) {
                    entry.attach();
                    entry.fadeIn(1.5f);
                }
            } else {
                log.debug("!(entryMap.containsKey(index)) index: " + index
                        + ")");
            }
        }
    }

    @Override
    protected final float getTextY(int position) {
        return textBeginY - TEXT_SPACER - FONT_SIZE * (position)
                - (float) FONT_SIZE / 2.f;
    }

    /**
     *
     * @param x
     * @param y
     * @param angle - angle in degree
     * @param width
     * @param height
     */
    @Override
    protected final void addSpacer(int x, int y, float angle, int width, int height) {
        Quaternion q = new Quaternion();
        q = q.fromAngleAxis(FastMath.DEG_TO_RAD * angle, new Vector3f(0, 0, 1));
        Quad spacerQuad = new Quad("Spacer", width, height);
        spacerQuad.setRenderState(tsSpacer);
        spacerQuad.setRenderState(JMEUtils.initalizeBlendState());
        spacerQuad.updateRenderState();
        spacerQuad.setLocalTranslation(x, y, 0);
        spacerQuad.setLocalRotation(q);
        GameTaskQueueManager.getManager().update(new AttachChildCallable(node, spacerQuad));
    }

    @Override
    public void close() {
        open = false;
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
        // Close animation
        st = CloseAnimation.getController(node, ANIMATION_DURATION, (float) getWidth(), (float) getHeight());
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
    }

    @Override
    public void open() {
        open = true;
        if (node.getControllers().contains(st)) {
            GameTaskQueueManager.getManager().update(new RemoveControllerCallable(node, st));
        }
        // Open animation
        st = OpenAnimation.getController(node, ANIMATION_DURATION);
        GameTaskQueueManager.getManager().update(new AddControllerCallable(node, st));
        update();
    }
    
    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean toggle(ActionName action) {
        if (isOpen()) {
            close();
            return false;
        } else {
            open();
            return true;
        }
    }
}