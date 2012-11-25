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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.VCARD;
import de.dfki.covida.covidacore.components.IVideoComponent;
import de.dfki.covida.covidacore.tw.TouchAndWriteComponentHandler;
import de.dfki.covida.covidacore.utils.AnnotationSearch;
import de.dfki.ermed.client.ERmedFacade;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which holds all {@link AnnotationData}.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class AnnotationStorage {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AnnotationData.class);
    /**
     * Instance of the {@link AnnotationStorage}
     */
    private static AnnotationStorage instance;
    /**
     * 
     */
    private ERmedFacade client;
    /**
     * Data list
     */
    private Map<UUID, AnnotationData> dataList;

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
     * Returns the to the given {@link IVideoComponent} linked
     * {@link AnnotationData}
     *
     * @param component {@link IVideoComponent}
     * @return {@link AnnotationData}
     */
    public AnnotationData getAnnotationData(IVideoComponent component) {
        if (!dataList.containsKey(component.getUUID())) {
            addNewComponent(component);
        }
        return dataList.get(component.getUUID());
    }

    /**
     * Adds a new {@link IVideoComponent}
     *
     * @param component {@link IVideoComponent} to add
     */
    private void addNewComponent(IVideoComponent component) {
        if (!dataList.containsKey(component.getUUID())) {
            AnnotationData data = AnnotationData.load(component);
            dataList.put(component.getUUID(), data);
        }
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
    public List<IVideoComponent> getVideo(AnnotationData data) {
        List<IVideoComponent> videos = new ArrayList<>();
        if (dataList.containsValue(data)) {
            for (UUID uuid : dataList.keySet()) {
                if (dataList.get(uuid).equals(data)) {
                    for (IVideoComponent video : TouchAndWriteComponentHandler
                            .getInstance().getVideos()) {
                        videos.add(video);
                    }
                }
            }
        }
        return videos;
    }

    /**
     * Search after ocurrence of the search terms in the annoatations.
     *
     * @param terms search terms
     * @return {@link Map} which contains the search results as mapping of
     * {@link AnnotationData} to {@link List} of {@link Annotation}s
     */
    public Map<AnnotationData, List<Annotation>> search(List<String> terms) {
        return AnnotationSearch.search(terms, getAnnotationDatas());
    }

    public void load(UUID uuid) {
        for (UUID dataUUID : dataList.keySet()) {
            AnnotationData data = dataList.get(dataUUID);
            if (data != null) {
                for (Annotation annotation : data.getAnnotations()) {
                    if (annotation.uuid.equals(uuid)) {
                        for (IVideoComponent video :
                                TouchAndWriteComponentHandler.getInstance()
                                .getVideos()) {
                            if (video.getUUID().equals(data.uuid)) {
                                video.load(annotation);
                            }
                        }
                        break;
                    }
                }
            }
        }

    }

    /**
     * Removes the annotation data which is refered by the {@link UUID}
     *
     * @param uuid {@link UUID} which represents the {@link AnnotationData}
     */
    public void remove(UUID uuid) {
        if (dataList.get(uuid).uuid.equals(uuid)) {
            dataList.remove(uuid);
        }
    }

    /**
     * Removes the annotation which is refered by the {@link UUID}
     *
     * @param uuid {@link UUID} which represents the {@link Annotation}
     */
    public void removeAnnotation(UUID uuid) {
        for (IVideoComponent video : TouchAndWriteComponentHandler.getInstance().getVideos()) {
            AnnotationData data = dataList.get(video.getUUID());
            if (data != null) {
                for (Annotation annotation : data.getAnnotations()) {
                    if (annotation.uuid.equals(uuid)) {
                        data.remove(annotation);
                        break;
                    }
                }
            }
        }
    }

    public void generateRDF() {
        DateAdapter dateAdapter = new DateAdapter();
        // create an empty model
        Model model = ModelFactory.createDefaultModel();
        for (AnnotationData data : getAnnotationDatas()) {
            Resource video = model.createResource();
            video.addProperty(DC.title, data.title);
            video.addProperty(DC.source, data.videoSource);
            for (Annotation annotation : data.getAnnotations()) {
                Resource creator = model.createResource();
                if (annotation.creator == null) {
                    for (PenData pen : CovidaConfiguration.getInstance().pens) {
                        if (pen.userlogin != null) {
                            annotation.creator = pen.userlogin;
                            break;
                        }
                    }
                    if (annotation.creator == null) {
                        annotation.creator = "default_user";
                    }
                }
                creator.addProperty(VCARD.NAME, annotation.creator);
                creator.addProperty(VCARD.CLASS, "User");
                String dat = "";
                if (annotation.date != null) {
                    try {
                        dat = dateAdapter.marshal(annotation.date);
                    } catch (Exception ex) {
                        log.error("", ex);
                    }
                }
                Resource annot = model.createResource(video);
                annot.addProperty(DC.creator, creator);
                annot.addProperty(DC.date, dat);
                annot.addProperty(DC.description, annotation.description);
                annot.addProperty(DC.creator, creator);
                annot.addProperty(DC.description, annotation.description);
                video.addProperty(DC.subject, annot);
            }

        }
        File file = new File("../covida-res/rdf.xml");
        log.debug("Write rdf to: " + file);
        FileWriter w = null;
        try {
            w = new FileWriter(file);
            model.write(w);
            log.debug("Written rdf to: " + file);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(AnnotationData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendAnnotation(AnnotationData data, Annotation annotation) {
        client.annotateVideo(data.videoSource, data.title, annotation.time_start, null,
                annotation.description.split(" "));
    }

}
