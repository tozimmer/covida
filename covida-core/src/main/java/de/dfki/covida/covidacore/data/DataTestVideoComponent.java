/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore.data;

import de.dfki.covida.covidacore.components.IVideoComponent;

/**
 *
 * @author Tobias
 */
public class DataTestVideoComponent implements IVideoComponent {

    @Override
    public String getSource() {
        return "../covida-res/videos/Collaborative Video Annotation.mp4";
    }

    @Override
    public String getTitle() {
        return "Covida Demo";
    }

    @Override
    public void load(Annotation annotation) {
    }
}
