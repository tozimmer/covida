/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidacore;

import com.sun.jna.NativeLibrary;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.covidacore.tw.TouchAndWriteSupport;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import de.dfki.touchandwrite.conf.TouchAndWriteConfiguration;
import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 *
 * @author Tobias
 */
public class MainImplementation {

    /**
     * Logger
     */
    protected static Logger log = Logger.getLogger(MainImplementation.class);
    ;
    protected static TouchAndWriteDevice device;
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

        if (null != VLC_SEARCH_PATH) {
            File folder = new File(VLC_SEARCH_PATH);
            if (!folder.exists() || !folder.isDirectory()) {
                log.error("Pleas install vlc in the directory " + VLC_SEARCH_PATH);
                System.exit(-1);
            }
            log.info("Adding vlc path: " + VLC_SEARCH_PATH);
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), VLC_SEARCH_PATH);
        } else {
            log.error("Pleas set vlc directory.");
            System.exit(-1);
        }
        System.setProperty("jna.dump_memory", DUMP_NATIVE_MEMORY);

        final CovidaCMDOptions opt = new CovidaCMDOptions();
        CmdLineParser parser = new CmdLineParser(opt);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("TouchAndWrite [options...] arguments...");
            parser.printUsage(System.err);
            System.exit(-1);
        }

        if (opt.isDebug()) {
            if (new File(opt.getLogfile()).canRead()) {
                DOMConfigurator.configure(new File(opt.getLogfile()).getAbsolutePath());
            } else {
                BasicConfigurator.configure();
            }
            log = Logger.getLogger(MainImplementation.class);
            log.debug("Verbose mode");
            log.debug("Current working directory: " + System.getProperty("user.dir"));
        } else {
            BasicConfigurator.configure();
            log = Logger.getLogger(MainImplementation.class);
            log.setLevel(Level.OFF);
        }
        TouchAndWriteConfiguration conf = TouchAndWriteConfiguration.getDefaultEEESlateConfig();
        if (opt.getDevice() == TouchAndWriteDevice.TW_TABLE) {
            conf = TouchAndWriteConfiguration.getDefaultTWTableConfig();
        }
        MainImplementation.device = opt.getDevice();
    }

    public TouchAndWriteDevice getDevice() {
        return device;
    }

    public void startApplication(final IApplication application) {
        Thread app = new Thread(new Runnable() {
            @Override
            public void run() {
                application.start();
            }
        });
        app.setName("Application Thread");
        app.start();
        TouchAndWriteSupport.start(application, getDevice());
    }
}
