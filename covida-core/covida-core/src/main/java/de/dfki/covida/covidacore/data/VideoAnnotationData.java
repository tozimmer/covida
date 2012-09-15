/*
 * VideoAnnotationData.java
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

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@XmlRootElement(name = "data")
public class VideoAnnotationData implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5408416424492049902L;
    @XmlElement(name = "videoSource")
    public String videoSource;
    @XmlElement(name = "title")
    public String title;
    @XmlElement(name = "annotation")
    @XmlElementWrapper(name = "annotations")
    public List<VideoAnnotation> annotations;
    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(VideoAnnotationData.class);

    public VideoAnnotationData() {
        annotations = new ArrayList<VideoAnnotation>();
    }

    public int size() {
        return annotations.size();
    }

    public void export() {
        /**
         * Create new document
         */
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        Document doc = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error(e);
        }
        if (docBuilder != null) {
            doc = docBuilder.newDocument();
        }
        if (doc != null) {
            /**
             * Creating the XML tree (anvil specification file)
             */
            //create the root element annotation-spec and add it to the document
            Element root_spec = doc.createElement("annotation-spec");
            doc.appendChild(root_spec);
            //head
            Element head_spec = doc.createElement("head");
            root_spec.appendChild(head_spec);
            Element valuetype = doc.createElement("valuetype-def");
            head_spec.appendChild(valuetype);
            Element valueset = doc.createElement("valueset");
            valueset.setAttribute("name", "phaseType");
            valuetype.appendChild(valueset);
            Element value = doc.createElement("value-el");
            value.setAttribute("color", "#eeee00");
            value.setTextContent("stroke");
            valueset.appendChild(value);
            //body
            Element body_spec = doc.createElement("body");
            root_spec.appendChild(body_spec);
            Element track_spec = doc.createElement("track-spec");
            value.setAttribute("name", "main");
            value.setAttribute("type", "primary");
            value.setAttribute("color-attr", "emphasis");
            Element track_doc = doc.createElement("doc");
            track_doc.setTextContent("foo");
            track_spec.appendChild(track_doc);
            body_spec.appendChild(track_spec);
            /**
             * Output the XML
             */
            //set up a transformer
            TransformerFactory transfac_spec = TransformerFactory.newInstance();
            Transformer trans_spec = null;
            try {
                trans_spec = transfac_spec.newTransformer();
            } catch (TransformerConfigurationException e) {
                log.error(e);
            }
            if (trans_spec != null) {
                trans_spec.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                trans_spec.setOutputProperty(OutputKeys.INDENT, "yes");
            }
            //create string from xml tree
            StringWriter sw_spec = new StringWriter();
            StreamResult result_spec = new StreamResult(sw_spec);
            DOMSource source_spec = new DOMSource(doc);
            try {
                trans_spec.transform(source_spec, result_spec);
            } catch (TransformerException e) {
                log.error(e);
            }
            String xmlString_spec = sw_spec.toString();
            //write xml
            try {
                // Create file 
                FileWriter fstream = new FileWriter(videoSource + "_spec.xlm");
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(xmlString_spec);
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                log.error(e);
            }

            /**
             * Create new document
             */
            dbfac = DocumentBuilderFactory.newInstance();
            try {
                docBuilder = dbfac.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                log.error(e);
            }
            doc = docBuilder.newDocument();

            /**
             * Creating the XML tree (anvil XML annotation file)
             */
            //create the root element annotation and add it to the document
            Element root = doc.createElement("annotation");
            doc.appendChild(root);
            //head
            Element head = doc.createElement("head");
            root.appendChild(head);
            Element specification = doc.createElement("specification");
            specification.setAttribute("src", videoSource + "_spec.xml");
            head.appendChild(specification);
            Element video = doc.createElement("video");
            video.setAttribute("src", videoSource);
            head.appendChild(video);
            //body
            Element body = doc.createElement("body");
            root.appendChild(body);
            Element track = doc.createElement("track");
            // TODO track name + attribute
            track.setAttribute("name", "main");
            track.setAttribute("type", "primary");
            body.appendChild(track);
            Element el;
            Element token;
            Element point;
            for (int i = 0; i < size(); i++) {
                el = doc.createElement("el");
                el.setAttribute("index", String.valueOf(i));
                el.setAttribute("start", String.valueOf(annotations.get(i).time_start));
                el.setAttribute("end", String.valueOf(annotations.get(i).time_end));
                track.appendChild(el);
                token = doc.createElement("token");
                token.setTextContent(annotations.get(i).description);
                el.appendChild(token);
                if (annotations.get(i).shapePoints == null) {
                    ShapePoints points = new ShapePoints();
                    points.add(new Point(0, 0));
                    annotations.get(i).shapePoints = points;
                    log.debug(
                            "data.shapePointsList.get(i) == null - Annotation: "
                            + i + "Video: " + videoSource);
                }
                for (int j = 0; j < annotations.get(i).shapePoints.size(); j++) {
                    point = doc.createElement("point");
                    point.setTextContent(annotations.get(i).shapePoints.get(j).x
                            + "," + annotations.get(i).shapePoints.get(j).y);
                    el.appendChild(point);
                }

            }
            /**
             * Output to the XML file
             */
            //set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = null;
            try {
                trans = transfac.newTransformer();
            } catch (TransformerConfigurationException e) {
                log.error(e);
            }
            if (trans != null) {
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
            }
            //create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            try {
                trans.transform(source, result);
            } catch (TransformerException e) {
                log.error(e);
            }
            String xmlString = sw.toString();
            //write XML file
            try {
                // Create file 
                FileWriter fstream = new FileWriter(videoSource + ".anvil");
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(xmlString);
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                log.error(e);
            }
        }
    }

    public void save() {
        JAXBContext jc;
        File file = new File(videoSource + ".xml");
        log.debug("Write data to: " + file);
        try {
            jc = JAXBContext.newInstance(VideoAnnotationData.class);
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

    public static VideoAnnotationData load(File file) {
        VideoAnnotationData instance;
        try {
            if (file != null && file.canRead()) {
                JAXBContext jc = JAXBContext.newInstance(VideoAnnotationData.class);
                Unmarshaller u = jc.createUnmarshaller();
                instance = (VideoAnnotationData) u.unmarshal(file);
                log.debug("Data file loaded at location: "
                        + file.getAbsolutePath());
            } else {
                log.debug("No data file exists at location: "
                        + file.getAbsolutePath());
                instance = new VideoAnnotationData();
            }
        } catch (JAXBException e) {
            log.debug(e + " create new VideoAnnotationData");
            instance = new VideoAnnotationData();
        }
        log.debug(instance);
        return instance;
    }

    /**
     *
     * @param id
     * @return true if removing was a success
     */
    public boolean remove(int id) {
        if (id > -1 && annotations.size() > id) {
            annotations.remove(id);
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Long> getTimeList() {
        ArrayList<Long> list = new ArrayList<Long>();
        for (VideoAnnotation annotation : annotations) {
            list.add(annotation.time_start);
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Size: " + annotations.size());
        buffer.append("\n");
        buffer.append("title: " + title);
        buffer.append("\n");
        buffer.append("videoSource: " + videoSource);
        buffer.append("\n");
        for (VideoAnnotation annotation : annotations) {
            buffer.append("times_start: " + annotation.time_start);
            buffer.append("\n");
            buffer.append("times_end: " + annotation.time_end);
            buffer.append("\n");
            buffer.append("shapePointsList: " + annotation.shapePoints);
            buffer.append("\n");
            buffer.append("descriptions: " + annotation.description);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
