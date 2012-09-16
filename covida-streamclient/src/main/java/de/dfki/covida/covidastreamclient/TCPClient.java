/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidastreamclient;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * TCP Client to receive images from the streaming server
 *
 * @author Tobias
 */
public class TCPClient extends Thread {

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(TCPClient.class);
    private Socket socket;
    private List<IRemoteReceiver> receivers;
    private boolean listening;
    private ObjectInputStream Sinput;

    // Constructor connection receiving a socket number
    public TCPClient(int port) {
        this.receivers = new ArrayList<>();
        // we use "localhost" as host name, the server is on the same machine
        // but you can put the "real" server name or IP address
        connect();
        try {
            Sinput = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            log.error(ex);
            log.error("Try to reconnect");
            connect();
        }
    }
    
    private void connect(){
        while (!listening) {
            try {
                socket = new Socket("localhost", 1500);
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
                    log.error(ex);
                    return;
                }
            }

        }
    }

    @Override
    public void run() {
        while (listening) {
            try {
                log.debug("Waiting for input");
                Object object = Sinput.readObject();
                if (object instanceof byte[]) {
                    for (IRemoteReceiver receiver : receivers) {
                        receiver.onNewFrame((byte[]) object);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error("Problem reading back from server: " + e);
                close();
            }
        }
        try {
            Sinput.close();
        } catch (IOException ex) {
            log.error(ex);
        }
    }

    public void addReceiver(IRemoteReceiver receiver) {
        this.receivers.add(receiver);
    }

    public final void close() {
        listening = false;
    }
}