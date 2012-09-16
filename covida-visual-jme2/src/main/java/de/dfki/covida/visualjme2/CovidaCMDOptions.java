/*
 * CovidaCMDOptions.java
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

import de.dfki.touchandwrite.TouchAndWriteDevice;
import org.kohsuke.args4j.Option;

/**
 * Options for VideoTouch.
 *
 * @author Tobias Zimmermann
 *
 */
public class CovidaCMDOptions {

    @Option(name = "-conf", usage = "Location of the log configuration.")
    private String configuration = "src/main/resources/apps/config.xml";
    @Option(name = "-d", usage = "Verbose output")
    private boolean debug;
    @Option(name = "-log", usage = "Location of the log configuration.")
    private String logfile = "log4j.xml";
    @Option(name = "-device", usage = "Name of the device")
    private String device;

    /**
     * Returns the location of the Touch and Write configuration file.
     *
     * @return
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * @return the logfile
     */
    public String getLogfile() {
        return logfile;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @return the debug
     */
    public TouchAndWriteDevice getDevice() {
        if (device != null) {
            return TouchAndWriteDevice.valueOf(device);
        } else {
            return TouchAndWriteDevice.EEE_SLATE;
        }
    }
}
