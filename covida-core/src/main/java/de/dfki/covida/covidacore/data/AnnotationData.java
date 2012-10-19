/*
 * AnnotationData.java
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
import de.dfki.covida.covidacore.data.test.DataTest;
import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class which handles the {@link Annotation}s.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
@XmlRootElement(name = "data")
public class AnnotationData implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049902L;
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AnnotationData.class);
    /**
     * Unique id
     */
    @XmlElement(name = "uuid")
    public final UUID uuid;
    /**
     * Video source as {@link String}
     */
    @XmlElement(name = "videoSource")
    public String videoSource;
    /**
     * Video title as {@link String}
     */
    @XmlElement(name = "title")
    public String title;
    /**
     * List of {@link Annotation}
     */
    @XmlElementWrapper(name = "annotations")
    @XmlElement(name = "annotation")
    private List<Annotation> annotations;

    /**
     * Creates a new instance of {@link AnnotationData}
     */
    private AnnotationData() {
        annotations = new ArrayList<>();
        this.uuid = UUID.randomUUID();
    }

    /**
     * Returns the amount of {@link Annotation}s.
     *
     * @return amount of {@link Annotation}s.
     */
    public int size() {
        return annotations.size();
    }

    /**
     * Exports {@link AnnotationData} to Anvil compatible format
     */
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
            log.error("", e);
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
                log.error("", e);
            }
            if (trans_spec != null) {
                trans_spec.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                trans_spec.setOutputProperty(OutputKeys.INDENT, "yes");
            }
            //create string from xml tree
            StringWriter sw_spec = new StringWriter();
            StreamResult result_spec = new StreamResult(sw_spec);
            DOMSource source_spec = new DOMSource(doc);
            if (trans_spec != null) {
                try {
                    trans_spec.transform(source_spec, result_spec);
                } catch (TransformerException e) {
                    log.error("", e);
                }
            }
            String xmlString_spec = sw_spec.toString();
            //write xml
            BufferedWriter out = null;
            try {
                // Create file
                FileWriter fstream = new FileWriter(videoSource + "_spec.xlm");
                out = new BufferedWriter(fstream);
                out.write(xmlString_spec);
            } catch (IOException e) {
                log.error("", e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        log.error("", ex);
                    }
                }
            }

            /**
             * Create new document
             */
            dbfac = DocumentBuilderFactory.newInstance();
            try {
                docBuilder = dbfac.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                log.error("", e);
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
                if (annotations.get(i).strokelist == null) {
                    Stroke points = new Stroke();
                    StrokeList strokes = new StrokeList();
                    points.points.add(new Point(0, 0));
                    strokes.strokelist.add(points);
                    annotations.get(i).strokelist = strokes;
                    log.debug(
                            "data.shapePointsList.get(i) == null - Annotation: "
                            + i + "Video: " + videoSource);
                }
                for (Stroke points : annotations.get(i).strokelist.strokelist) {
                    for (Point p : points.points) {
                        point = doc.createElement("point");
                        point.setTextContent(p.x + "," + p.y);
                        el.appendChild(point);
                    }
                }

            }
            /**
             * Output to the XML file
             */
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = null;
            try {
                trans = transfac.newTransformer();
            } catch (TransformerConfigurationException e) {
                log.error("", e);
            }
            if (trans != null) {
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
            }
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            if (trans != null) {
                try {
                    trans.transform(source, result);
                } catch (TransformerException e) {
                    log.error("", e);
                }
            }
            String xmlString = sw.toString();
            try {
                FileWriter fstream = new FileWriter(videoSource + ".anvil");
                out = new BufferedWriter(fstream);
                out.write(xmlString);
            } catch (IOException e) {
                log.error("", e);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    log.error("Closing stream failed: ", ex);
                }
            }
        }
    }

    /**
     * Savves the annotation data to a XML file.
     */
    public void write() {
        JAXBContext jc;
        File file = new File(videoSource + ".xml");
        log.debug("Write data to: " + file);
        FileWriter w = null;
        try {
            jc = JAXBContext.newInstance(AnnotationData.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            w = new FileWriter(file);
            m.marshal(this, w);
            m.toString();
            log.debug(
                    "Written data to: " + file);
        } catch (JAXBException | IOException e) {
            log.error("", e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException ex) {
                    log.error("", ex);
                }
            }
        }
    }

    public void generateRDF() {
        DateAdapter dateAdapter = new DateAdapter();
        // create an empty model
        Model model = ModelFactory.createDefaultModel();

        Resource creator = model.createResource();
        creator.addProperty(VCARD.NAME, creator);
        creator.addProperty(VCARD.CLASS, "User");

        Resource video = model.createResource();
        video.addProperty(DC.title, title);
        video.addProperty(DC.source, videoSource);
        Resource annot = model.createResource(video);
        annot.addProperty(DC.creator, creator);
        for (Annotation annotation : annotations) {
            try {
                annot.addProperty(DC.date, dateAdapter.marshal(annotation.date));
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(DataTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            annot.addProperty(DC.description, annotation.description);
            annot.addProperty(DC.subject, "");
            video.addProperty(DC.subject, annot);
            annot = model.createResource(video);
            annot.addProperty(DC.creator, creator);
            try {
                annot.addProperty(DC.date, dateAdapter.marshal(annotation.date));
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(DataTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            annot.addProperty(DC.description, annotation.description);
            annot.addProperty(DC.subject, "");
            video.addProperty(DC.subject, annot);
        }
        File file = new File(videoSource + ".xml");
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

    /**
     * Removes the {@link Annotation} with the given id.
     *
     * @param id {@link UUID} of the {@link Annotation}
     * @return true if removing was a success
     */
    public boolean remove(UUID id) {
        for (Annotation annotation : annotations) {
            if (annotation.uuid.equals(id)) {
                annotations.remove(annotation);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove {@link Annotation}
     *
     * @param annotation {@link Annotation}
     * @return true if removing was successfull
     */
    public boolean remove(Annotation annotation) {
        return annotations.remove(annotation);
    }

    /**
     * Returns the identifier list of {@link Annotation} als {@link List} of
     * {@link Long}.
     *
     * @return {@link List} of {@link Long}.
     */
    public ArrayList<Long> getTimeList() {
        ArrayList<Long> list = new ArrayList<>();
        for (Annotation annotation : annotations) {
            list.add(annotation.time_start);
        }
        return list;
    }

    /**
     * Returns the {@link List} of {@link Annotation}s
     *
     * @return {@link List} of {@link Annotation}s
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * Loads {@link AnnotationData} of {@link IVideoComponent}
     *
     * Note that the {@link AnnotationData} will be load from xml if a xml file
     * on the location {@code component.getSource() + ".xml"} exists.
     *
     * @param component {@link IVideoComponent}
     * @return {@link AnnotationData}
     */
    public static AnnotationData load(IVideoComponent component) {
        File file = new File(component.getSource() + ".xml");
        AnnotationData instance;
        try {
            if (file != null && file.canRead()) {
                JAXBContext jc = JAXBContext.newInstance(AnnotationData.class);
                Unmarshaller u = jc.createUnmarshaller();
                instance = (AnnotationData) u.unmarshal(file);

                log.debug(
                        "Data file loaded at location: {}",
                        file.getAbsolutePath());
            } else {
                log.debug("No data file exists, create new VideoAnnotationData");
                instance = new AnnotationData();
                instance.videoSource = component.getSource();
                instance.title = component.getTitle();
            }
        } catch (JAXBException e) {
            log.debug("XML parsing error: {}, create new VideoAnnotationData", e);
            instance = new AnnotationData();
            instance.videoSource = component.getSource();
            instance.title = component.getTitle();
        }
        return instance;
    }

    /**
     * Saves the {@link Annotation}
     *
     * Note that existing {@link Annotation} with the same {@link UUID} will be
     * overwritten.
     *
     * @param annotation {@link Annotation}
     */
    public void save(Annotation annotation) {
        for (Annotation entry : annotations) {
            if (entry.uuid.equals(annotation.uuid)) {
                annotations.remove(entry);
                break;
            }
        }
        annotations.add(annotation);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Size: ").append(annotations.size());
        buffer.append("\n");
        buffer.append("title: ").append(title);
        buffer.append("\n");
        buffer.append("videoSource: ").append(videoSource);
        buffer.append("\n");
        for (Annotation annotation : annotations) {
            buffer.append("times_start: ").append(annotation.time_start);
            buffer.append("\n");
            buffer.append("times_end: ").append(annotation.time_end);
            buffer.append("\n");
            buffer.append("shapePointsList: ").append(annotation.strokelist);
            buffer.append("\n");
            buffer.append("descriptions: ").append(annotation.description);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
