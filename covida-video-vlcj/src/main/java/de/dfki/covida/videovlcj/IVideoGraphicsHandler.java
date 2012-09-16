/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.videovlcj;

import de.dfki.covida.covidacore.data.ShapePoints;

/**
 *
 * @author Tobias
 */
public interface IVideoGraphicsHandler {
 
    public ShapePoints getDrawing();

    public void clearShape();

    public ShapePoints getSavedShape();

    public void setShape(ShapePoints points);

    public void setHWR(String hwr);

    public void clearDrawing();
    
}
