/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualswing;

import de.dfki.covida.covidacore.MainImplementation;
import org.apache.log4j.Logger;

/**
 *
 * @author Tobias
 */
public class Covida {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(Covida.class);
   

    public static void main(String[] args) {
        MainImplementation main = new MainImplementation(args);
        main.startApplication(new MainFrame("Covida"));
    }

    
}
