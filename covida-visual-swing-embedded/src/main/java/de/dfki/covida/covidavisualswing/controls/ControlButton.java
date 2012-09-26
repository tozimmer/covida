/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualswing.controls;

import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.utils.ActionName;
import java.awt.Component;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

/**
 *
 * @author Tobias
 */
public class ControlButton extends JToggleButton {

    public ControlButton(URL imageURL, URL imageSelectedURL, ActionName action, 
            IControlableComponent component, int width, int height) {
        super(new ControlAction(action, component));
        setName(action.toString());
        
        setIcon(new ImageIcon(((new ImageIcon(imageURL)).getImage())
                .getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH),
                action.toString()));
        setSelectedIcon(new ImageIcon(((new ImageIcon(imageSelectedURL)).getImage())
                .getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH),
                action.toString()));
        setSize(width, height);
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}
