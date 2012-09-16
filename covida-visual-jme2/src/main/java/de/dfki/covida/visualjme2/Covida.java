/*
 * Covida.java
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
package de.dfki.covida.visualjme2;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import de.dfki.touchandwrite.conf.TouchAndWriteConfiguration;
import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * Covida main application.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class Covida {

    /**
     * Logger
     */
    private static Logger log;
    /**
     * Covida visual application
     */
    private static CovidaApplication application;

    public static void main(final String[] args) {
        Thread.currentThread().setName("Covida Visual");
        log = Logger.getLogger(Covida.class);
        String vlcPath = "vlc";
        File folder = new File(vlcPath);
        if (!folder.exists() || !folder.isDirectory()) {
            log.error("Pleas install vlc in the directory " + vlcPath);
            System.exit(-1);
        }
        log.info("Adding vlc path ...");
        NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(), "../vlc");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
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
            log = Logger.getLogger(Covida.class);
            log.debug("Verbose mode");
            log.debug("Current working directory: " + System.getProperty("user.dir"));
        } else {
            BasicConfigurator.configure();
            log = Logger.getLogger(Covida.class);
            log.setLevel(Level.OFF);
        }
        TouchAndWriteConfiguration conf = TouchAndWriteConfiguration.getDefaultEEESlateConfig();
        if (opt.getDevice() == TouchAndWriteDevice.TW_TABLE) {
            conf = TouchAndWriteConfiguration.getDefaultTWTableConfig();
        }
        application = new CovidaApplication(opt.getDevice(), "Covida");
        application.start();
    }
}
