/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2;

import de.dfki.touchandwrite.TouchAndWriteDevice;
import org.apache.log4j.Logger;

/**
 *
 * @author tzimmermann
 */
public class ApplicationImpl extends AbstractApplication {

    /**
     * {@link TouchAndWriteDevice}
     */
    private final TouchAndWriteDevice device;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(ApplicationImpl.class);

    /**
     * Creates an instance of {@link ApplicationImpl}
     *
     * @param device {@link TouchAndWriteDevice}
     * @param windowtitle window title for the application
     */
    public ApplicationImpl(TouchAndWriteDevice device, String windowtitle) {
        super();
        this.device = device;
        this.windowtitle = windowtitle;
    }

    /**
     * Loading animation
     *
     * Note that method have to be overridden for a loading animation.
     */
    protected void loadingAnimation() {
    }

    @Override
    protected void simpleInitGame() {
        
        loadingAnimation();
    }

    @Override
    public void setBackground() {
    }

    @Override
    public String getWindowTitle() {
        return windowtitle;
    }

    
}
