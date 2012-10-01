/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidastreamclient;

import java.awt.Dimension;

/**
 *
 * @author Tobias
 */
public interface IRemoteReceiver {
    
    public void onNewFrame(byte[] bytes);

    public void setScreenSize(Dimension dimension);
    
}
