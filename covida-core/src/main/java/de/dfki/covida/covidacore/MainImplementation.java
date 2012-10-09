/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore;

import com.sun.jna.NativeLibrary;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.covidacore.tw.TouchAndWriteSupport;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import java.io.File;
import java.util.logging.Level;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 *
 * @author Tobias
 */
public class MainImplementation {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(MainImplementation.class);
    private TouchAndWriteDevice device;
    /**
     * Log level, used only if the -Dvlcj.log= system property has not already
     * been set.
     */
    private static final String VLCJ_LOG_LEVEL = "INFO";
    /**
     * Change this to point to your own vlc installation, or comment out the
     * code if you want to use your system default installation. <p> This is a
     * bit more explicit than using the -Djna.library.path= system property.
     */
    private static final String VLC_SEARCH_PATH = "../covida-res/vlc";
    /**
     * Set to true to dump out native JNA memory structures.
     */
    private static final String DUMP_NATIVE_MEMORY = "false";

    public MainImplementation(String[] args) {
        Thread.currentThread().setName("Covida Visual");
        if (null == System.getProperty("vlcj.log")) {
            System.setProperty("vlcj.log", VLCJ_LOG_LEVEL);
        }

        try {
            File folder = new File(VLC_SEARCH_PATH);
            if (!folder.exists() || !folder.isDirectory()) {
                log.error("Pleas install vlc in the directory " + VLC_SEARCH_PATH);
                throw new RuntimeException();
            }
            log.info("Adding vlc path: " + VLC_SEARCH_PATH);
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), VLC_SEARCH_PATH);
        } catch (NullPointerException e) {
            throw new RuntimeException(e + "Please set vlc directory.");
        }
        System.setProperty("jna.dump_memory", DUMP_NATIVE_MEMORY);

        setLoggin(false);

        final CovidaCMDOptions opt = new CovidaCMDOptions();
        CmdLineParser parser = new CmdLineParser(opt);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            log.error("TouchAndWrite [options...] arguments... " + e);
            throw new RuntimeException();
        }

        if (opt.isDebug()) {
        } else {
        }
        device = opt.getDevice();
    }

    /**
     * Returns current TW device
     *
     * @return {@link TouchAndWriteDevice}
     */
    public TouchAndWriteDevice getDevice() {
        return device;
    }

    /**
     * Sets the logging status.
     *
     * @param activated if true the logging for {@link uk.co.caprica.vlcj} is
     * activated.
     */
    public final void setLoggin(Boolean activated) {
        if (!activated) {
            java.util.logging.Logger.getLogger("uk.co.caprica.vlcj").setLevel(java.util.logging.Level.OFF);
        } else {
            java.util.logging.Logger.getLogger("uk.co.caprica.vlcj").setLevel(java.util.logging.Level.ALL);
        }
    }

    /**
     * Starts Application
     *
     * @param application {@link IApplication}
     */
    public void startApplication(IApplication application) {
        Thread app;
        app = new Thread(new ApplicationThread(application));
        app.setName("Application Thread");
        app.start();
        while(!application.isReady()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.error("", ex);
            }
        }
        TouchAndWriteSupport.start(application, getDevice());
    }

    private static class ApplicationThread implements Runnable {

        private final IApplication application;

        private ApplicationThread(IApplication application) {
            this.application = application;
        }

        @Override
        public void run() {
            application.start();
        }
    }
}
