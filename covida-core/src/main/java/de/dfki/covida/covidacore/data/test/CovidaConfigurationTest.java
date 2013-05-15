/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data.test;

import de.dfki.covida.covidacore.data.CovidaConfiguration;

/**
 *
 * @author touchandwrite
 */
public class CovidaConfigurationTest {
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        CovidaConfiguration conf = CovidaConfiguration.getInstance();
        conf.loadMediaData();
        conf.save();
        conf = CovidaConfiguration.load();
    }
    
}
