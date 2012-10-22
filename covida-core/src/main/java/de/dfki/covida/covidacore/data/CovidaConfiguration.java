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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
     * Indicates if the application should be automatacaly log on with default
     * login
     *
     * @see #defaultlogin
     */
    @XmlElement(name = "autologon")
    public Boolean autologon;
    /**
     * If autologon is true this login is used
     *
     * @see #autologon
     */
    @XmlElement(name = "defaultlogin")
    public String defaultlogin;
    /**
     * Paths to the video resources as {@link List} of {@link VideoMediaData}.
     */
    @XmlElement(name = "video")
    @XmlElementWrapper(name = "videos")
    public List<VideoMediaData> videos = new ArrayList<>();
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
        autologon = false;
        defaultlogin = "Covida User";

        VideoMediaData data = new VideoMediaData();
        data.videoName = "CoVidA Demo";
        data.videoSource = "..\\covida-res\\videos\\Collaborative Video Annotation.mp4";
        data.time_start = 0;
        data.time_end = 0;
        data.repeat = true;
        data.width = -1;
        data.height = -1;
        videos.add(data);

        data = new VideoMediaData();
        data.videoName = "RadSpeech";
        data.videoSource = "..\\covida-res\\videos\\RadSpeech DFKI(360p_H.264-AAC).mp4";
        data.time_start = 0;
        data.time_end = 0;
        data.repeat = true;
        data.width = -1;
        data.height = -1;
        videos.add(data);

        pens.add(PenData.getDefaultConfig(null));
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
        File file = new File("../covida-res/config.xml");
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
     * @param location {@link String} which represents the configuration XML
     * file.
     * @return {@link CovidaConfiguration}
     */
    public static CovidaConfiguration load() {
        File file = new File("../covida-res/config.xml");
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
        for (VideoMediaData data : instance.videos) {
            if (data.uuid == null) {
                data.uuid = UUID.randomUUID();
            }
        }
        return instance;
    }

    public static String getLoggedUser(String penID) {
        if (penID == null) {
            if (CovidaConfiguration.getInstance().pens.isEmpty()) {
                String defaultLogin = CovidaConfiguration.getInstance().defaultlogin;
                if (defaultLogin != null) {
                    return defaultLogin;
                } else {
                    return "default user";
                }
            } else {
                String login = CovidaConfiguration.getInstance().pens.get(0).userlogin;
                if (login == null) {
                    login = "default user";
                }
                return login;
            }
        }
        String user = null;
        for (PenData pen : CovidaConfiguration.getInstance().pens) {
            if (pen.id != null && pen.id.equals(penID)) {
                user = pen.userlogin;
                break;
            }
        }
        if (user == null) {
            for (PenData pen : CovidaConfiguration.getInstance().pens) {
                if (pen.id == null) {
                    pen.id = penID;
                    user = pen.userlogin;
                    break;
                }
            }
        }
        if (user == null) {
            PenData pen = PenData.getDefaultConfig(penID);
            pen.userlogin = CovidaConfiguration.getInstance().defaultlogin;
            CovidaConfiguration.getInstance().pens.add(pen);
        }
        return user;
    }

    public void setUser(String id, String login) {
        boolean set = false;
        for (PenData pen : pens) {
            if (pen.id != null && pen.id.equals(id)) {
                pen.userlogin = login;
                set = true;
                break;
            }
        }
        if (!set) {
            if (pens.isEmpty()) {
                PenData pen = PenData.getDefaultConfig(id);
                pen.userlogin = login;
                pens.add(pen);
                set = true;
            } else {
                for (PenData pen : pens) {
                    if (pen.id == null) {
                        pen.userlogin = login;
                        set = true;
                        break;
                    }
                }
            }
        }
        if (!set) {
            PenData pen = PenData.getDefaultConfig(id);
            pen.userlogin = login;
            pens.add(pen);
        }
        save();
    }
}
