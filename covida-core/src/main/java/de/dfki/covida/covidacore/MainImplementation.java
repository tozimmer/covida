/*
 * MainImplementation.java
 *
 * Copyright (c) 2012, Tobias Zimmermann All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.dfki.covida.covidacore;

import com.sun.jna.NativeLibrary;
import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.covida.covidacore.data.ermed.ERmedClient;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.covidacore.tw.TouchAndWriteSupport;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import java.io.File;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * Main implementation
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class MainImplementation {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(MainImplementation.class);
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
        log.debug("Register on ERMed Proxy");
        try {
            ERmedClient.getInstance();
        } catch (Exception ex) {
            log.warn("ERmedClient couldn't be initialized.");
        }
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
        CovidaConfiguration.getInstance().device = opt.getDevice();
    }

    /**
     * Returns current TW device
     *
     * @return {@link TouchAndWriteDevice}
     */
    public TouchAndWriteDevice getDevice() {
        return CovidaConfiguration.getInstance().device;
    }

    /**
     * Sets the logging status.
     *
     * @param activated if true the logging for vlcj is activated.
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
        while (!application.isReady()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.error("", ex);
            }
        }
        TouchAndWriteSupport.start(application);
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
