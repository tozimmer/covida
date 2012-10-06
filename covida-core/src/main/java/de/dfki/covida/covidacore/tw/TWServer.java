/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.tw;

import de.dfki.touchandwrite.TouchAndWriteDevice;
import de.dfki.touchandwrite.TouchAndWriteServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias
 */
public class TWServer {
    private final TouchAndWriteServer twserver;
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(TWServer.class);
    
    public TWServer(TouchAndWriteDevice device){
        //Starting touch and write server
        twserver = new TouchAndWriteServer(device);
    }
    
    public void start(){
        new Thread(twserver, "Touch&Write").start();
        // Inprocess mode
        while (!twserver.running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("" + e);
            }
        }
    }
}
