/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author touchandwrite
 */
@XmlRootElement(name = "annotationclasslist")
public class AnnotationClassList implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049902L;
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AnnotationData.class);
    @XmlElementWrapper(name = "annotationclasses")
    @XmlElement(name = "annotationclass")
    public List<AnnotationClass> annotationClasses;
    
    private AnnotationClassList(){
        annotationClasses = new ArrayList<>();
        AnnotationClass annotationClass = new AnnotationClass();
        annotationClass.color = Color.decode("0x104E8B");
        annotationClass.name = "Person";
        annotationClasses.add(annotationClass);
        annotationClass = new AnnotationClass();
        annotationClass.color = Color.decode("0x6E8B3D");
        annotationClass.name = "Object";
        annotationClasses.add(annotationClass);
        annotationClass = new AnnotationClass();
        annotationClass.color = Color.decode("0x8B2323");
        annotationClass.name = "Logo";
        annotationClasses.add(annotationClass);
        annotationClass = new AnnotationClass();
        annotationClass.color = Color.decode("0xA2B5CD");
        annotationClass.name = "Text";
        annotationClasses.add(annotationClass);
        annotationClass = new AnnotationClass();
        annotationClass.color = Color.decode("0xCD3700");
        annotationClass.name = "Emotion";
        annotationClasses.add(annotationClass);
        annotationClass = new AnnotationClass();
        annotationClass.color = Color.decode("0xEEC900");
        annotationClass.name = "Animal";
        annotationClasses.add(annotationClass);
    }

    /**
     * Savves the annotation data to a XML file.
     */
    public void write() {
        JAXBContext jc;
        File file = new File("../covida-res/classes.xml");
        log.debug("Write data to: " + file);
        FileWriter w = null;
        try {
            jc = JAXBContext.newInstance(AnnotationClassList.class);
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
    
    public static AnnotationClassList load() {
        File file = new File("../covida-res/classes.xml");
        AnnotationClassList instance;
        try {
            if (file != null && file.canRead()) {
                JAXBContext jc = JAXBContext.newInstance(AnnotationClassList.class);
                Unmarshaller u = jc.createUnmarshaller();
                instance = (AnnotationClassList) u.unmarshal(file);
                log.debug(
                        "Data file loaded at location: {}",
                        file.getAbsolutePath());
            } else {
                log.debug("No data file exists, create new VideoAnnotationData");
                instance = new AnnotationClassList();
                instance.write();
            }
        } catch (JAXBException e) {
            log.debug("XML parsing error: {}, create new VideoAnnotationData", e);
            instance = new AnnotationClassList();
            instance.write();
        }
        return instance;
    }
}
