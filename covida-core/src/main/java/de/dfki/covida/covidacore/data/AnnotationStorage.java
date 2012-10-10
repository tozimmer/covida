/*
 * AnnotationStorage.java
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
package de.dfki.covida.covidacore.data;

import de.dfki.covida.covidacore.components.IVideoComponent;
import de.dfki.covida.covidacore.utils.AnnotationSearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class which holds all {@link AnnotationData}.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class AnnotationStorage {

    private static AnnotationStorage instance;
    private Map<IVideoComponent, AnnotationData> dataList;

    /**
     * Private constructor of {@link AnnotationStorage}
     */
    private AnnotationStorage() {
        dataList = new HashMap<>();
    }

    /**
     * Returns the instance of {@link AnnotationStorage}
     *
     * @return {@link AnnotationStorage}
     */
    public synchronized static AnnotationStorage getInstance() {
        if (instance == null) {
            instance = new AnnotationStorage();
        }
        return instance;
    }

    /**
     * Adds {@link AnnotationData} to the storage.
     *
     * @param component {@link IVideoComponent} which is linked to this
     * {@link AnnotationData}
     * @param annotation {@link Annotation}
     */
    public void addAnnotation(IVideoComponent component, Annotation annotation) {
        if (dataList.containsKey(component)) {
            dataList.get(component).annotations.add(annotation);
        } else {
            addNewComponent(component);
            dataList.get(component).annotations.add(annotation);
        }
    }

    /**
     * Returns the to the given {@link IVideoComponent} linked
     * {@link AnnotationData}
     *
     * @param component {@link IVideoComponent}
     * @return {@link AnnotationData}
     */
    public AnnotationData getAnnotationData(IVideoComponent component) {
        if (!dataList.containsKey(component)) {
            addNewComponent(component);
        }
        return dataList.get(component);
    }

    private void addNewComponent(IVideoComponent component) {
        AnnotationData data = AnnotationData.load(component);
        dataList.put(component, data);
    }

    /**
     * Returns a {@link Iterable} of all stored {@link AnnotationData}.
     *
     * @return {@link Iterable} of {@link AnnotationData}
     */
    public Iterable<AnnotationData> getAnnotationDatas() {
        return dataList.values();
    }

    /**
     * Returns the to the given {@link AnnotationData} linked
     * {@link IVideoComponent}
     *
     * @param data {@link AnnotationData}
     * @return {@link IVideoComponent}
     * @return {@code null} if the {@link IVideoComponent} is not registered.
     */
    public IVideoComponent getVideo(AnnotationData data) {
        if (dataList.containsValue(data)) {
            for (IVideoComponent video : dataList.keySet()) {
                if (dataList.get(video).equals(data)) {
                    return video;
                }
            }
        }
        return null;
    }

    public Map<AnnotationData, List<Annotation>> search(List<String> terms) {
        return AnnotationSearch.search(terms, getAnnotationDatas());
    }

    public void load(UUID uuid) {
        for (IVideoComponent video : dataList.keySet()) {
            for (Annotation annotation : dataList.get(video).getAnnotations()) {
                if (annotation.uuid.equals(uuid)) {
                    video.load(annotation);
                }
            }
        }
    }
}
