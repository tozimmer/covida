/*
 * TCPClient.java
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
package de.dfki.covida.covidaflvcreator.client;

import de.dfki.covida.covidaflvcreator.utils.CreationRequest;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP Client to send {@link CreationRequest}s to the video creation server and
 * receive file names of the created videos from the video creation server.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public abstract class TCPClient extends Thread {

    /**
     * Logger
     */
    protected static final Logger log = LoggerFactory.getLogger(TCPClient.class);
    /**
     * System time when request started
     */
    protected long start;
    /**
     * {@link Socket}
     */
    private Socket socket;
    /**
     * If true the client is listening
     */
    private boolean listening;
    /**
     * {@link ObjectInputStream}
     */
    private ObjectInputStream Sinput;
    /**
     * {@link ObjectOutputStream}
     */
    private ObjectOutputStream Soutput;
    /**
     * {@link Integer} which represents the port number on which the client
     * listens
     */
    private final int port;

    /**
     * Constructor connection receiving a socket number
     *
     * @param port socket port number
     */
    public TCPClient(int port) {
        this.port = port;
        connect();
        try {
            Sinput = new ObjectInputStream(socket.getInputStream());
            Soutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            log.error("", ex);
            log.error("Try to reconnect");
            connect();
        }
    }

    /**
     * Connects to the server
     */
    private void connect() {
        while (!listening) {
            try {
                socket = new Socket("localhost", port);
                listening = true;
                log.debug("Connection accepted " + socket.getInetAddress() + ":"
                        + socket.getPort());
            } catch (Exception e) {
                try {
                    log.error("Error connectiong to server:" + e);
                    log.error("Waiting for connection.");
                    listening = false;
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    log.error("", ex);
                    return;
                }
            }

        }
    }

    /**
     * Sends a request
     *
     * @param request {@link CreationRequest}
     */
    public void writeRequest(CreationRequest request) {
        try {
            start = System.currentTimeMillis();
            Soutput.reset();
            Soutput.writeObject(request);
        } catch (IOException e) {
            log.error("Exception writing request: " + e);
            log.error("Closing connection");
            listening = false;
        }
    }

    /**
     * Method invokes the closing of the server connection.
     */
    public final void close() {
        listening = false;
    }

    @Override
    public void run() {
        while (listening) {
            try {
                Object object = Sinput.readObject();
                if (object instanceof String) {
                    onNewVideoCreated((String) object);
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error("Problem reading back from server: " + e);
                close();
            }

        }
        try {
            Sinput.close();
        } catch (IOException ex) {
            log.error("", ex);
        }
    }

    /**
     * Must be implement to use the filename in the client application.
     *
     * @param filename {@link String} which represents the file location
     */
    public abstract void onNewVideoCreated(String filename);
}
