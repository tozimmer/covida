/*
 * TouchAndWriteSupport.java
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
package de.dfki.covida.covidacore.tw;

import de.dfki.covida.covidacore.data.CovidaConfiguration;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import de.dfki.touchandwrite.conf.TouchAndWriteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tobias
 */
public class TouchAndWriteSupport {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(TouchAndWriteConfiguration.class);

    /**
     * Starts the {@link TouchAndWriteSupport}
     *
     * @param application {@link IApplication}
     * @param device {@link TouchAndWriteDevice}
     */
    public static void start(IApplication application) {
        TouchAndWriteConfiguration conf = TouchAndWriteConfiguration
                .getDefaultWMInputConfig(application.getScreenSize(),
                "localhost", "localhost");
        TouchAndWriteDevice device = CovidaConfiguration.getInstance().device;
        if (device.equals(TouchAndWriteDevice.WMINPUT) || 
                device.equals(TouchAndWriteDevice.EEE_SLATE)) {
            conf = TouchAndWriteConfiguration
                    .getDefaultWMInputConfig(application.getScreenSize(),
                    "localhost", "localhost");
        } else if (device.equals(TouchAndWriteDevice.TUIO) ||
                device.equals(TouchAndWriteDevice.TW_TABLE)) {
            conf = TouchAndWriteConfiguration
                    .getDefaultTUIOConfig(application.getScreenSize(),
                    "localhost", "localhost", new int[0]);
        }
        TWServer twServer = new TWServer(conf);
        twServer.start();
//        config.getEventmanagerConfig().setHost("192.168.83.100");
        TouchAndWriteEventHandler touchAndWrite = new TouchAndWriteEventHandler(
                application, conf);
        log.debug("Starting Touch&Write support.");
        touchAndWrite.start();
    }
}
