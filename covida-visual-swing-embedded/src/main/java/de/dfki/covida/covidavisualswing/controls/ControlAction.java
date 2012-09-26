/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualswing.controls;

import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.utils.ActionName;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author Tobias
 */
public class ControlAction extends AbstractAction {

    private final ActionName action;
    private final IControlableComponent component;

    public ControlAction(ActionName action, IControlableComponent component) {
        this.action = action;
        this.enabled = true;
        this.component = component;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        component.toggle(action);
    }
}
