package de.dfki.touchandwrite;

import com.sun.jna.NativeLibrary;
import de.dfki.covida.Covida;
import de.dfki.touchandwrite.analyser.pen.PenDataAnalyser;
import de.dfki.touchandwrite.analyser.touch.TouchInputAnalyser;
import de.dfki.touchandwrite.anoto.pen.AnotoPenInteractionComponent;
import de.dfki.touchandwrite.application.ApplicationProfile;
import de.dfki.touchandwrite.conf.TouchAndWriteConfiguration;
import de.dfki.touchandwrite.event.EventManagerServer;
import de.dfki.touchandwrite.input.pen.PenInputDevice;
import de.dfki.touchandwrite.input.touch.TouchInputDevice;
import de.dfki.touchandwrite.replay.TouchReplayerDevice;
import de.dfki.touchandwrite.tuio.TUIOTouchInputDevice;
import de.dfki.touchandwrite.win7.Win7Input;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * Main class for the Touch and Write server system.
 *
 *
 * @author Markus Weber
 *
 */
public class Covida_eeeslate implements Runnable {

    /**
     * Logger
     */
    private static Logger log;
    /**
     * Command line options.
     */
    private TouchAndWriteCMDOptions opt;
    /**
     * Configuration for the Touch and Write system
     */
    private static TouchAndWriteConfiguration conf;
    /**
     * Implementation which is responsible for pen interaction.
     */
    private PenInputDevice penDevice;
    /**
     * Implementation which is responsible for touch interaction.
     */
    private TouchInputDevice touchDevice;
    /**
     * Implementation which is responsible for replaying logged touches.
     */
    private TouchReplayerDevice touchReplayerDevice;
    /**
     * Eventmanager server.
     */
    private EventManagerServer eventManager;
    /**
     * Runtime flag
     */
    private boolean running = false;
    static Covida_eeeslate twserver;

    /**
     * Constructor
     *
     * @param opt
     */
    public Covida_eeeslate(TouchAndWriteCMDOptions opt) {
        this.opt = opt;
    }

    /**
     * Initializes the subcomponents of the system.
     */
    protected void init() {
        if (opt.getTouchAndWriteConfiguration() != null) {
            if (new File(opt.getTouchAndWriteConfiguration()).canRead()) {
                log.debug("Loading Touch And Write configuration. [Configuration location]="
                        + opt.getTouchAndWriteConfiguration());
                conf = TouchAndWriteConfiguration.loadSettings(new File(opt.getTouchAndWriteConfiguration()));
            } else if (getClass().getClassLoader().getResource(opt.getTouchAndWriteConfiguration()) != null) {
                log.debug("Loading Touch And Write configuration. [Configuration location (Resources)]="
                        + opt.getTouchAndWriteConfiguration());
                conf = TouchAndWriteConfiguration.loadSettings(getClass().getClassLoader().getResource(opt.getTouchAndWriteConfiguration()));
            } else {
                log.debug("Loading Touch And Write configuration [Default EEE Slate Config]");
                conf = TouchAndWriteConfiguration.getDefaultEEESlateConfig();
            }
        } else if (getClass().getClassLoader().getResource("conf/touchandwrite-eee-slate.xml") != null) {
            log.debug("Loading Touch And Write configuration. [Configuration location (Resources)]="
                    + opt.getTouchAndWriteConfiguration());
            conf = TouchAndWriteConfiguration.loadSettings(getClass().getClassLoader().getResource("conf/touchandwrite-eee-slate.xml"));
        } else {
            log.debug("Loading Touch And Write configuration [Default EEE Slate Config]");
            conf = TouchAndWriteConfiguration.getDefaultEEESlateConfig();
        }
        List<ApplicationProfile> profiles = new ArrayList<>();
        if (opt.getLocationApplicationProfiles() != null) {
            File appProfileDir = new File(opt.getLocationApplicationProfiles());
            if (appProfileDir.exists()) {
                for (File profile : appProfileDir.listFiles()) {
                    if (profile.isFile() && profile.getName().endsWith("profile")) {
                        log.debug("Loading application profile. [location]="
                                + profile.getAbsolutePath());
                        profiles.add(ApplicationProfile.loadAppProfile(profile));
                    }
                }
            } else {
                profiles.add(ApplicationProfile.loadAppProfile(getClass().getClassLoader().getResource(opt.getTouchAndWriteConfiguration())));
            }
        } else if (opt.getApplicationProfile() != null) {
            profiles.add(ApplicationProfile.loadAppProfile(new File(opt.getApplicationProfile())));
        } else {
            profiles.add(ApplicationProfile.loadAppProfile(getClass().getClassLoader().getResource("profiles/default.profile")));
        }
        this.eventManager = EventManagerServer.getEventManagerServer(conf.getEventmanagerConfig());
        if (opt.isPenServer() || opt.isAll()) {
            try {
                if ("WM_PEN".equalsIgnoreCase(conf.getPenInputDevice().getDevicetype())) { // Either WM_PEN
                    penDevice = Win7Input.getInstance(conf);
                } else { // or Anoto
                    penDevice = new AnotoPenInteractionComponent(conf);
                }
                this.eventManager.addPenInputDevice(penDevice, new PenDataAnalyser(penDevice, this.conf,
                        profiles, this.eventManager));
                log.info("Starting pen interaction...");
            } catch (Exception e) {
                log.error(e);
            }
        }
        if (opt.isTouchServer() || opt.isAll() && conf.getTouchInputDevice() != null) {
            try {
                if ("WM_TOUCH".equalsIgnoreCase(conf.getTouchInputDevice().getDevicetype())) {
                    touchDevice = Win7Input.getInstance(conf);
                } else if ("TUIO".equalsIgnoreCase(conf.getTouchInputDevice().getDevicetype())) {
                    touchDevice = new TUIOTouchInputDevice(conf);
                }

                this.eventManager.addTouchInputDevice(touchDevice, new TouchInputAnalyser(touchDevice, this.conf,
                        this.eventManager));
                log.info("Starting touch interaction...");
            } catch (Exception e) {
                log.error(e);
            }
        }

        if (opt.isReplayTouchLog() || opt.isAll()
                && opt.getReplayTouchLogFile() != null) {
            try {
                // TODO Reflection
                touchReplayerDevice = new TouchReplayerDevice(opt.getReplayTouchLogFile());

                this.eventManager.addTouchInputDevice(touchReplayerDevice, null);
                log.info("Going to replay touches in file: "
                        + opt.getReplayTouchLogFile() + " ...");
            } catch (Exception e) {
                log.error(e);
            }
        }
        this.eventManager.startUp();
        running = true;
    }

    public static void main(String[] args) {
        NativeLibrary.addSearchPath("libvlc", "C:\\Program Files (x86)\\VideoLAN\\VLC");
        TouchAndWriteCMDOptions opt = new TouchAndWriteCMDOptions();
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
            log = Logger.getLogger(Covida_eeeslate.class);
            log.debug("Verbose mode");
            log.debug("Current working directory: " + System.getProperty("user.dir"));
        } else {
            BasicConfigurator.configure();
            log = Logger.getLogger(Covida_eeeslate.class);
            log.setLevel(Level.OFF);
        }
        // Starting touch and write server
        twserver = new Covida_eeeslate(opt);
        new Thread(twserver, "Touch&Write").start();
        // Inprocess mode


        while (!twserver.running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }

        Runnable app = new Runnable() {

            @Override
            public void run() {
                final String[] arguments = {"-conf",
                    "C:\\CoVidA\\covida-eee-slate.xml"};
                Covida app = new Covida(conf, arguments);
                app.setJmeLoggin(false);
                app.start();
            }
        };
        new Thread(app).start();
    }

    @Override
    public void run() {
        init();
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }
}
