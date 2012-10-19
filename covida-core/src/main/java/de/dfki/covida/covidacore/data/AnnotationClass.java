/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data;

import java.awt.Color;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author touchandwrite
 */
@XmlRootElement(name = "annotationclass")
public class AnnotationClass {
    
    @XmlElement(name = "name")
    public String name;
    @XmlElement(name = "color")
    @XmlJavaTypeAdapter(ColorAdapter.class)
    public Color color;
}

