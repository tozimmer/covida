/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data;

import java.awt.Color;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter to serialize {@link Color}.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ColorAdapter extends XmlAdapter<String, Color> {

    @Override
    public Color unmarshal(String s) {
        return Color.decode(s);
    }

    @Override
    public String marshal(Color c) {
        return "#" + Integer.toHexString(c.getRGB());
    }
}
