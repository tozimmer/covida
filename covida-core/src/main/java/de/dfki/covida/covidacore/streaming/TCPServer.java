package de.dfki.covida.covidacore.streaming;

import java.awt.Dimension;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * This is to help people to write Client server application I tried to make it
 * as simple as possible... the client connect to the server the client send a
 * String to the server the server returns it in UPPERCASE thats all
 */
public class TCPServer extends Thread {

    /**
     * Logger
     */
    private static Logger log = Logger.getLogger(TcpThread.class);
    private static TCPServer instance;
    // the socket used by the server
    private ServerSocket serverSocket;
    // server constructor
    private static int port = 1500;
    private List<TcpThread> tcpThreads;
    public Dimension dimension;

    private TCPServer() {
        tcpThreads = new ArrayList<>();
    }

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

    public void writeByteBuffer(ByteBuffer buffer, int width, int height, int depth) {
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

    public void setScreenSize(Dimension dimension) {
        this.dimension = dimension;
    }

    public static TCPServer getInstance() {
        if (instance == null) {
            instance = new TCPServer();
        }
        return instance;
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
    class TcpThread extends Thread {
        // the socket where to listen/talk

        Socket socket;
        public boolean running;
        private ObjectOutputStream Soutput;

        TcpThread(Socket socket) {
            this.socket = socket;
            this.running = true;
        }

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
                log.error(ex);
                running = false;
                try {
                    Soutput.flush();
                    Soutput.close();
                } catch (IOException ex1) {
                    log.error(ex1);
                }
            }
            while (running) {
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ex) {
                    log.error(ex);
                }
            }
        }
    }
}
