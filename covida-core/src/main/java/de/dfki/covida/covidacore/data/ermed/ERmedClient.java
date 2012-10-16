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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author touchandwrite
 */
public class ERmedClient {

    private static ERmedClient instance;
    private ERmedFacade client;

    private ERmedClient() {
        if (AppProperties.getInstance().getProperty("ermedactivated")
                .equals("true")) {
            client = ERmedFacade.newInstance();
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
                .equals("true")) {
            client.annotateVideo(data.title, annotation.time_start, 
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
            Logger.getLogger(ERmedClient.class.getName()).log(Level.SEVERE, w.toString());
            return w.toString();
        } catch (JAXBException ex) {
            Logger.getLogger(ERmedClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
