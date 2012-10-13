/*
 * TCPServer.java
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
package de.dfki.covida.covidaflvcreator.server;

import de.dfki.covida.covidaflvcreator.AnnotatedVideoCreator;
import de.dfki.covida.covidaflvcreator.IVideoReceiver;
import de.dfki.covida.covidaflvcreator.utils.CreationRequest;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP server
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class TCPServer extends Thread {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(TcpThread.class);
    /**
     * the socket used by the server
     */
    private ServerSocket serverSocket;
    /**
     * the port used by the server
     */
    private int port;
    /**
     * {@link List} of all active {@link TcpThread}
     */
    private List<TcpThread> tcpThreads;

    /**
     * Private constructor of {@link TCPServer}
     */
    public TCPServer(int port) {
        this.port = port;
        tcpThreads = new ArrayList<>();
    }

    /**
     * Sends the file location as {@link String}
     *
     * @param filename file location as {@link String}
     */
    public void sendFileName(String filename) {
        List<TcpThread> deadThreads = new ArrayList<>();
        for (TcpThread tcpThread : tcpThreads) {
            if (tcpThread.running) {
                tcpThread.writeFileName(filename);
            } else {
                deadThreads.add(tcpThread);
            }
        }
        for (TcpThread deadThread : deadThreads) {
            tcpThreads.remove(deadThread);
            deadThread = null;
        }
    }

    @Override
    public void run() {
        /* create socket server and wait for connection requests */
        try {
            serverSocket = new ServerSocket(port);
            log.debug("#########################################################");
            log.debug("# Server waiting for client on port " + serverSocket.getLocalPort() + " #");
            log.debug("#########################################################");

            while (true) {
                Socket socket = serverSocket.accept();  // accept connection
                log.debug("New client asked for a connection");
                TcpThread t = new TcpThread(socket);    // make a thread of it
                log.debug("Starting a thread for a new Client");
                t.start();
                tcpThreads.add(t);
            }
        } catch (IOException e) {
            log.error("Exception on new ServerSocket: " + e);
        }
    }

    /**
     * One instance of this thread will run for each client
     */
    class TcpThread extends Thread implements IVideoReceiver {

        /**
         * the socket where to listen/talk.
         */
        private Socket socket;
        /**
         * Output stream
         */
        private ObjectOutputStream Soutput;
        /**
         * Indicades if client is running.
         */
        public boolean running;
        private ObjectInputStream Sinput;

        /**
         * Creates an instance of {@link TcpThread}
         *
         * @param socket
         */
        public TcpThread(Socket socket) {
            this.socket = socket;
            this.running = true;
        }

        /**
         * Sends a String
         *
         * @param filename {@link String}
         */
        public void writeFileName(String filename) {
            try {
                Soutput.reset();
                Soutput.writeObject(filename);
            } catch (IOException e) {
                log.error("Exception writing  String: " + e);
                log.error("Closing connection");
                running = false;
            }
        }

        @Override
        public void run() {
            Thread.currentThread().setName("TCP-Server");
            try {
                Soutput = new ObjectOutputStream(socket.getOutputStream());
                Sinput = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                log.error("", ex);
                running = false;
                try {
                    Soutput.flush();
                    Soutput.close();
                } catch (IOException ex1) {
                    log.error("", ex1);
                }
            }
            while (running) {
                try {
                    Object object = Sinput.readObject();
                    if (object instanceof CreationRequest) {
                        CreationRequest request = (CreationRequest) object;
                        AnnotatedVideoCreator creator =
                                new AnnotatedVideoCreator(request.filename,
                                this);
                        creator.setIntervall(request.timeStart, request.timeEnd);
                        creator.setShape(request.shape);
                        creator.setText(request.label);
                        Thread creatorThread = new Thread(creator);
                        creatorThread.setName("Creator Thread");
                        creatorThread.start();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    log.error(" " + e);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    log.error("", ex);
                }
            }
        }

        @Override
        public void setVideoFile(String file) {
            writeFileName(file);
        }
    }
}
