/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.components;

import de.dfki.covida.covidacore.data.Annotation;
import java.util.UUID;

/**
 *
 * @author Tobias
 */
public interface IVideoComponent {
    
    public String getSource();

    public String getTitle();

    public void load(Annotation annotation);
    
    
}
