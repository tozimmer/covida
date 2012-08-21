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
package de.dfki.covida.data;

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
import org.apache.log4j.Logger;

@XmlRootElement(name = "configuration")
public class CovidaConfiguration implements Serializable {

    private CovidaConfiguration() {
        texturePath = "media/textures/";
        videos.add(new VideoData());
        videos.get(videos.size() - 1).enabled = true;
        videos.get(videos.size() - 1).repeating = true;
        videos.get(videos.size() - 1).position = 0;
        videos.get(videos.size() - 1).size = 25;
        videos.get(videos.size() - 1).time_end = 0;
        videos.get(videos.size() - 1).time_start = 0;
        videos.add(new VideoData());
        videos.get(videos.size() - 1).enabled = true;
        videos.get(videos.size() - 1).repeating = true;
        videos.get(videos.size() - 1).position = 1;
        videos.get(videos.size() - 1).size = 25;
        videos.get(videos.size() - 1).time_end = 0;
        videos.get(videos.size() - 1).time_start = 0;
//		videos.add(new VideoData());
//		videos.get(videos.size()-1).enabled = true;
//		videos.get(videos.size()-1).repeating = true;
//		videos.get(videos.size()-1).position = 2;
//		videos.get(videos.size()-1).size = 25;
//		videos.get(videos.size()-1).time_end = 0;
//		videos.get(videos.size()-1).time_start = 0;
//		videos.add(new VideoData());
//		videos.get(videos.size()-1).enabled = true;
//		videos.get(videos.size()-1).repeating = true;
//		videos.get(videos.size()-1).position = 3;
//		videos.get(videos.size()-1).size = 25;
//		videos.get(videos.size()-1).time_end = 0;
//		videos.get(videos.size()-1).time_start = 0;
//		videos.add(new VideoData());
//		videos.get(videos.size()-1).enabled = true;
//		videos.get(videos.size()-1).repeating = true;
//		videos.get(videos.size()-1).position = 4;
//		videos.get(videos.size()-1).size = 25;
//		videos.get(videos.size()-1).time_end = 0;
//		videos.get(videos.size()-1).time_start = 0;
//		videos.add(new VideoData());
//		videos.get(videos.size()-1).enabled = true;
//		videos.get(videos.size()-1).repeating = true;
//		videos.get(videos.size()-1).position = 5;
//		videos.get(videos.size()-1).size = 25;
//		videos.get(videos.size()-1).time_end = 0;
//		videos.get(videos.size()-1).time_start = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "AScatch";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\test1.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
        videoSources.add(new VideoMediaData());
        videoSources.get(videoSources.size() - 1).videoName = "T&W Demo";
        videoSources.get(videoSources.size() - 1).videoSource = "videos\\test2.mp4";
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "Urban Planing";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\test3.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "RadSpeech";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\test4.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
        videoSources.add(new VideoMediaData());
        videoSources.get(videoSources.size() - 1).videoName = "RoboCup";
        videoSources.get(videoSources.size() - 1).videoSource = "videos\\test5.mp4";
        videoSources.get(videoSources.size() - 1).time_start = 0;
        videoSources.get(videoSources.size() - 1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "XML3D";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\test7.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "AILA - Project SemProM";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\AILA - Project SemProM.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "Robotics Innovation";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\Robotics Innovation Center 2010.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "Asguard II - Snow Run";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\Asguard II - Snow Run.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "Calisto";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\Calisto - Collaborative Media Exchange Terminal for the Internet of Services.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "KomParse";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\KomParse - Sprechende Agenten im virtuellen Berlin.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "Inveritas";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\Inveritas - Relative Navigation and Capture.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "Magnet Crawler";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\Magnet Crawler.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "Roboter AILA";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\Roboter AILA des DFKI auf der Hannover Messe 2010.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "RoDes";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\RoDes.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "Text 2.0";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\Text 2.0.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "AI Poker";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\A.I. Poker im Casino Virtuell - DFKI Intelligent User Interfaces.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
//		videoSources.add(new VideoMediaData());
//		videoSources.get(videoSources.size()-1).videoName = "IMoby";
//		videoSources.get(videoSources.size()-1).videoSource = "C:\\videos\\IMoby - Speed Control vs. Wheel Synchronisation.mp4";
//		videoSources.get(videoSources.size()-1).time_start = 0;
//		videoSources.get(videoSources.size()-1).time_end = 0;
        PenData pen = new PenData();
        pen.penColor = Color.WHITE;
        pen.penThickness = 1;
        pens.add(pen);
    }
    /**
     *
     */
    private static final long serialVersionUID = 729584990456758771L;
    private static final Logger log = Logger.getLogger(CovidaConfiguration.class);
    private static CovidaConfiguration instance;
    @XmlElement(name = "path")
    public String texturePath;
    @XmlElement(name = "videoSource")
    @XmlElementWrapper(name = "videoSourceList")
    public List<VideoMediaData> videoSources = new ArrayList<VideoMediaData>();
    @XmlElement(name = "video")
    @XmlElementWrapper(name = "videoList")
    public List<VideoData> videos = new ArrayList<VideoData>();
    @XmlElement(name = "pen")
    @XmlElementWrapper(name = "penList")
    public List<PenData> pens = new ArrayList<PenData>();

    public static CovidaConfiguration getInstance() {
        if (instance == null) {
            instance = new CovidaConfiguration();
        }
        return instance;
    }

    public void save() {
        JAXBContext jc;
        File file = new File("config.xml");
        log.debug("Write data to: " + file);
        try {
            jc = JAXBContext.newInstance(CovidaConfiguration.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            FileWriter w = new FileWriter(file);
            m.marshal(this, w);
            log.debug("Written data to: " + file);
        } catch (JAXBException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static CovidaConfiguration load(File file) {
        try {
            if (file != null && file.canRead()) {
                JAXBContext jc = JAXBContext.newInstance(CovidaConfiguration.class);
                Unmarshaller u = jc.createUnmarshaller();
                instance = (CovidaConfiguration) u.unmarshal(file);
                log.debug("Data file loaded at location: "
                        + file.getAbsolutePath());
            } else {
                log.debug("No data file exists at location: "
                        + file.getAbsolutePath());
                instance = CovidaConfiguration.getInstance();
            }
        } catch (JAXBException e) {
            log.debug(e + " create new VideoAnnotationData");
            instance = CovidaConfiguration.getInstance();
        }
        log.debug(instance);
        return instance;
    }
}
