package de.dfki.covida.covidavisualjme3;
 
import de.dfki.covida.covidacore.MainImplementation;
 
/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class Covida {
 
    public static void main(String[] args){
        MainImplementation main = new MainImplementation(args);
        main.startApplication(new MainApplication("Covida"));
    }

}