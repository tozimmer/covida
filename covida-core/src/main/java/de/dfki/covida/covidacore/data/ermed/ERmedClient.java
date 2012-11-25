/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data.ermed;

import de.dfki.covida.covidacore.data.Annotation;
import de.dfki.covida.covidacore.data.AnnotationData;
import de.dfki.covida.covidacore.data.StrokeList;
import de.dfki.ermed.client.ERmedFacade;
import de.dfki.ermed.utils.AppProperties;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author touchandwrite
 */
public class ERmedClient {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ERmedClient.class);
    private static ERmedClient instance;
    private ERmedFacade client;
    
    private ERmedClient() {
        if (AppProperties.getInstance().getProperty("ermedactivated")
                .equals("true")) {
            try {
                client = ERmedFacade.newInstance();
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
    }
    
    public static ERmedClient getInstance() {
        if (instance == null) {
            instance = new ERmedClient();
        }
        return instance;
    }
    
    public void sendAnnotation(AnnotationData data, Annotation annotation) {
        if (AppProperties.getInstance().getProperty("ermedactivated")
                .equals("true") && client != null) {
            client.annotateVideo(data.videoSource, annotation.time_start,
                    encode(annotation.strokelist),
                    annotation.description.split(" "));
        }
    }
    
    public static String encode(StrokeList shapes) {
        Writer w = new StringWriter();
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(StrokeList.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(shapes, w);
            log.debug(w.toString());
            return w.toString();
        } catch (JAXBException ex) {
            log.error("", ex);
        }
        return null;
    }
}
