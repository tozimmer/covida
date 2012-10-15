/*
 * CovidaConfiguration.java
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

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Covida configuration class.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
@XmlRootElement(name = "configuration")
public class CovidaConfiguration implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049666L;
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CovidaConfiguration.class);
    /**
     * Instance of {@link CovidaConfiguration}
     */
    private static CovidaConfiguration instance;
    /**
     * Texture path as {@link String}
     */
    @XmlElement(name = "path")
    public String texturePath;
    /**
     * Paths to the video resources as {@link List} of {@link VideoMediaData}.
     */
    @XmlElement(name = "videoSource")
    @XmlElementWrapper(name = "videoSourceList")
    public List<VideoMediaData> videoSources = new ArrayList<>();
    /**
     * Video data as {@link List} of {@link VideoData}
     */
    @XmlElement(name = "video")
    @XmlElementWrapper(name = "videoList")
    public List<VideoData> videos = new ArrayList<>();
    /**
     * List of pen configurations as {@link List} of {@link PenData}
     */
    @XmlElement(name = "pen")
    @XmlElementWrapper(name = "penList")
    public List<PenData> pens = new ArrayList<>();

    /**
     * Private constructor of {@link CovidaConfiguration}.
     */
    private CovidaConfiguration() {
        texturePath = "media/textures/";

        videos.add(new VideoData());
        videos.get(videos.size() - 1).enabled = true;
        videos.get(videos.size() - 1).repeating = true;
        videos.get(videos.size() - 1).position = 0;
        videos.get(videos.size() - 1).size = 25;
        videos.get(videos.size() - 1).time_end = 0;
        videos.get(videos.size() - 1).time_start = 0;
        videoSources.add(new VideoMediaData());
        videoSources.get(videoSources.size() - 1).videoName = "CoVidA Demo";
        videoSources.get(videoSources.size() - 1).videoSource = "..\\covida-res\\videos\\Collaborative Video Annotation.mp4";
        videoSources.get(videoSources.size() - 1).time_start = 0;
        videoSources.get(videoSources.size() - 1).time_end = 0;

        videos.add(new VideoData());
        videos.get(videos.size() - 1).enabled = true;
        videos.get(videos.size() - 1).repeating = true;
        videos.get(videos.size() - 1).position = 0;
        videos.get(videos.size() - 1).size = 25;
        videos.get(videos.size() - 1).time_end = 0;
        videos.get(videos.size() - 1).time_start = 0;
        videoSources.add(new VideoMediaData());
        videoSources.get(videoSources.size() - 1).videoName = "RadSpeech";
        videoSources.get(videoSources.size() - 1).videoSource = "..\\covida-res\\videos\\RadSpeech DFKI(360p_H.264-AAC).mp4";
        videoSources.get(videoSources.size() - 1).time_start = 0;
        videoSources.get(videoSources.size() - 1).time_end = 0;

        PenData pen = new PenData();
        pen.penColor = Color.WHITE;
        pen.penThickness = 1;
        pens.add(pen);
    }

    /**
     * Returns the instance of {@link CovidaConfiguration}.
     *
     * @return {@link CovidaConfiguration}
     */
    public synchronized static CovidaConfiguration getInstance() {
        if (instance == null) {
            instance = new CovidaConfiguration();
        }
        return instance;
    }

    /**
     * Saves the {@link CovidaConfiguration} as XML file.
     *
     * Note that the {@link CovidaConfiguration} is saved to "covida.xml"
     */
    public void save() {
        JAXBContext jc;
        File file = new File("config.xml");
        log.debug("Write data to: " + file);
        FileWriter w = null;
        try {
            jc = JAXBContext.newInstance(CovidaConfiguration.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            w = new FileWriter(file);
            m.marshal(this, w);
            log.debug("Written data to: " + file);
        } catch (JAXBException | IOException e) {
            log.error("", e);
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (IOException ex) {
                log.error("FileWriter closing failed: ", ex);
            }
        }
    }

    /**
     * Loads a {@link CovidaConfiguration}.
     *
     * @param file {@link File} which represents the configuration XML file.
     * @return {@link CovidaConfiguration}
     */
    public static CovidaConfiguration load(File file) {
        try {
            if (file != null && file.canRead()) {
                JAXBContext jc = JAXBContext.newInstance(CovidaConfiguration.class);
                Unmarshaller u = jc.createUnmarshaller();
                instance = (CovidaConfiguration) u.unmarshal(file);
                log.debug("Data file loaded at location: "
                        + file.getAbsolutePath());
            } else {
                log.debug("No data file exists at given location");
                instance = CovidaConfiguration.getInstance();
            }
        } catch (JAXBException e) {
            log.debug(e + " create new VideoAnnotationData");
            instance = CovidaConfiguration.getInstance();
        }
        log.debug("", instance);
        return instance;
    }
}
