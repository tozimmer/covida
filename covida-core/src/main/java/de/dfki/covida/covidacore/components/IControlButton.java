/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.components;

/**
 *
 * @author Tobias
 */
public interface IControlButton {
    
    public void setActive(boolean activated);
    
    public boolean getActive();
    
    public void setEnabled(boolean enabled);
    
    public boolean getEnabled();
    
    public void toggle();
}
