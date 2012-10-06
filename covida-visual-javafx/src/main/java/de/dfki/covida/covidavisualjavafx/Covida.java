/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualjavafx;

import de.dfki.covida.covidacore.MainImplementation;

public class Covida {
 
    public static void main(String[] args){
        MainImplementation main = new MainImplementation(args);
        main.startApplication(new App());
    }

}
