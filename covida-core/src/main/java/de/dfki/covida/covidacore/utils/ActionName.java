/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.utils;

/**
 *
 * @author Tobias
 */
public enum ActionName {

    NONE, BACKWARD, STOP, PLAYPAUSE, FORWARD, CLOSE, CHANGEMEDIA, SOUND, LIST, 
    SAVE, DELETE;

    @Override
    public String toString() {
        //only capitalize the first letter
        String s = super.toString();
        return s.substring(0, 1) + s.substring(1).toLowerCase() + " control button";
    }
}
