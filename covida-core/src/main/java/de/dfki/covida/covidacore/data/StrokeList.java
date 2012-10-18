/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 *
 * @author touchandwrite
 */
public class StrokeList implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5408416424492049610L;
    /**
     * List of {@link Point}
     */
    @XmlElementWrapper(name = "strokes")
    @XmlElement(name = "stroke")
    public List<Stroke> strokes;
    
    public StrokeList(){
        strokes = new ArrayList<>();
    }
}
