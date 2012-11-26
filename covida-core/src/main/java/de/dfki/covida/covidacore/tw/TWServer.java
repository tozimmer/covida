/*
 * TWServer.java
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
import de.dfki.touchandwrite.TouchAndWriteServer;
import de.dfki.touchandwrite.conf.TouchAndWriteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Component which initialize the Touch & Write server.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class TWServer extends Thread{
    private final TouchAndWriteServer twserver;
    /**
     * Logger
     */
    private Logger log = LoggerFactory.getLogger(TWServer.class);
    
    /**
     * Creates a new {@link TWServer} instance and {@link TouchAndWriteServer}
     * 
     * @param device 
     */
    public TWServer(TouchAndWriteConfiguration conf){
        log.debug("Create new Touch & Write server instance.");
        twserver = new TouchAndWriteServer(CovidaConfiguration
                .getInstance().device, conf);
    }
    
    /**
     * Starts the {@link TouchAndWriteServer} instance as new {@link Thread}
     */
    @Override
    public void start(){
        log.debug("Start Touch & Write server thread.");
        Thread.currentThread().setName("Touch & Write server control");
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Thread twServerThread = new Thread(twserver, "Touch & Write Server");
        twServerThread.setName("Touch & Write Server");
        twServerThread.start();
        // Inprocess mode
        while (!twserver.running) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error("" + e);
            }
        }
    }
}
