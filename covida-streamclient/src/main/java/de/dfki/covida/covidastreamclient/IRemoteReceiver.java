/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidastreamclient;

/**
 *
 * @author Tobias
 */
public interface IRemoteReceiver {
    
    public void onNewFrame(byte[] bytes);
    
}
