/*
 * TouchAndWriteCMDOptions.java
 * 
 * Copyright (c) 2012, Markus Weber All rights reserved.
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
package de.dfki.touchandwrite;

import org.kohsuke.args4j.Option;

/**
 * Options for Touch and Write Server.
 *
 * @author Markus Weber
 *
 */
public class TouchAndWriteCMDOptions {

    @Option(name = "-p", usage = "Start pen server.")
    private boolean penServer;
    @Option(name = "-t", usage = "Start touch server server.")
    private boolean touchServer;
    @Option(name = "-r", usage = "Replay logged touches.")
    private boolean replayTouchLog;
    @Option(name = "-rl", usage = "The file containing the logged touches to be replayed.")
    private String replayTouchLogFile;
    @Option(name = "-d", usage = "Verbose output")
    private boolean debug;
    @Option(name = "-log", usage = "Location of the log configuration.")
    private String logfile = "log4j.xml";
    @Option(name = "-a", usage = "Start all server.")
    boolean all = true;
    @Option(name = "-conf", usage = "Location of the log configuration.")
    private String configuration = null;
    @Option(name = "-profile", usage = "Location of the application profile.")
    private String applicationprofile = null;
    @Option(name = "-profiles", usage = "Location of the application profile directory.")
    private String locationApplicationProfiles = null;

    /**
     * @return the debug
     */
    public final boolean isDebug() {
        return debug;
    }

    /**
     * @return the penServer
     */
    public final boolean isPenServer() {
        return penServer;
    }

    /**
     * @return the gestureServer
     */
    public final boolean isTouchServer() {
        return touchServer;
    }

    /**
     * @return the replayTouchLog
     */
    public boolean isReplayTouchLog() {
        return replayTouchLog;
    }

    /**
     * @return the replayTouchLogFile
     */
    public String getReplayTouchLogFile() {
        return replayTouchLogFile;
    }

    /**
     * @return the all
     */
    public final boolean isAll() {
        return all;
    }

    /**
     * @return the logfile
     */
    public final String getLogfile() {
        return logfile;
    }

    /**
     * Returns the location of the Touch and Write configuration file.
     *
     * @return
     */
    public String getTouchAndWriteConfiguration() {
        return configuration;
    }

    /**
     * Returns the localtion of the application profile.
     *
     * @return the applicationprofile
     */
    public String getApplicationProfile() {
        return applicationprofile;
    }

    /**
     * @param locationApplicationProfiles the locationApplicationProfiles to set
     */
    public void setLocationApplicationProfiles(
            String locationApplicationProfiles) {
        this.locationApplicationProfiles = locationApplicationProfiles;
    }

    /**
     * @return the locationApplicationProfiles
     */
    public String getLocationApplicationProfiles() {
        return locationApplicationProfiles;
    }
}
