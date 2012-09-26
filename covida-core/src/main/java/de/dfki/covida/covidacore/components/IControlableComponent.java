/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.components;

import de.dfki.covida.covidacore.utils.ActionName;

/**
 *
 * @author Tobias
 */
public interface IControlableComponent {
    
    public boolean toggle(ActionName action);
    
    public int getWidth();
    
    public int getHeight();
    
    public int getId();
    
}
