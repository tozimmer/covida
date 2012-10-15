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
package de.dfki.covida.covidacore.streaming;

import java.awt.Dimension;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCP Server
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class TCPServer extends Thread {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(TcpThread.class);
    /**
     * {@link TCPServer} instance
     */
    private static TCPServer instance;
    /**
     * the socket used by the server
     */
    private ServerSocket serverSocket;
    /**
     * the port used by the server
     */
    private static int port = 1500;
    /**
     * {@link List} of all active {@link TcpThread}
     */
    private List<TcpThread> tcpThreads;
    /**
     * {@link Dimension} of the image frame.
     */
    public Dimension dimension;

    /**
     * Private constructor of {@link TCPServer}
     */
    private TCPServer() {
        tcpThreads = new ArrayList<>();
    }

    /**
     * Sends a {@link Array} of {@link Byte}
     *
     * @param bytes {@link Array} of {@link Byte}
     */
    public synchronized void writeByteBuffer(byte[] bytes) {
        List<TcpThread> deadThreads = new ArrayList<>();
        for (TcpThread tcpThread : tcpThreads) {
            if (tcpThread.running) {
                tcpThread.writeByteBuffer(bytes);
                bytes = null;
            } else {
                deadThreads.add(tcpThread);
            }
        }
        for (TcpThread deadThread : deadThreads) {
            tcpThreads.remove(deadThread);
            deadThread = null;
        }
    }

    /**
     * Converts {@link ByteBuffer} to an {@link Array} of {@link Byte}.
     *
     * @param buffer {@link ByteBuffer} which holds the image frame data
     * @param width Width of the image frame
     * @param height Height of the image frame
     * @param depth Color depth of the image
     */
    public void writeByteBuffer(ByteBuffer buffer, int width, int height,
            int depth) {
        int buffSize = depth * width * height;
        byte[] bytes = new byte[buffSize];
        for (int i = 0; i < buffSize; i++) {
            bytes[i] = buffer.get(i);
        }
        List<TcpThread> deadThreads = new ArrayList<>();
        for (TcpThread tcpThread : tcpThreads) {
            if (tcpThread.running) {
                tcpThread.writeByteBuffer(bytes);
                bytes = null;
            } else {
                deadThreads.add(tcpThread);
            }
        }
        for (TcpThread deadThread : deadThreads) {
            tcpThreads.remove(deadThread);
            deadThread = null;
        }
        buffer.clear();
    }

    /**
     * Sets the image frame {@link Dimension}
     *
     * @param dimension {@link Dimension}
     */
    public void setScreenSize(Dimension dimension) {
        this.dimension = dimension;
    }

    /**
     * Returns the instance of {@link TCPServer}.
     *
     * @return {@link TCPServer}
     */
    public synchronized static TCPServer getInstance() {
        if (instance == null) {
            instance = new TCPServer();
        }
        return instance;
    }

    /**
     * Create socket server and wait for connection requests
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            log.debug("Streaming TCP Server waiting for client on port "
                    + serverSocket.getLocalPort() + " #");

            while (true) {
                Socket socket = serverSocket.accept();
                log.debug("New client asked for a connection");
                TcpThread t = new TcpThread(socket);
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
    class TcpThread extends Thread {

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
         * Sends a {@link Array} of {@link Byte}.
         *
         * @param bytes {@link Byte}
         */
        public void writeByteBuffer(byte[] bytes) {
            try {
                Soutput.reset();
                Soutput.writeObject(bytes);
            } catch (IOException e) {
                log.error("Exception writing  Image: " + e);
                log.error("Closing connection");
                running = false;
            }
        }

        @Override
        public void run() {
            Thread.currentThread().setName("TCP-Server");
            try {
                Soutput = new ObjectOutputStream(socket.getOutputStream());
                Soutput.writeObject(dimension);
                Soutput.reset();
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
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    log.error("", ex);
                }
            }
        }
    }
}
