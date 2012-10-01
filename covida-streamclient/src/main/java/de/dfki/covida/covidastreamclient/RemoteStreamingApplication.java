/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidastreamclient;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tobias
 */
public class RemoteStreamingApplication implements IRemoteReceiver{
    private static RemoteStreamingApplication instance;
    private List<IStreamingClient> clients;
    private final TCPClient tcpClient;
    
    private RemoteStreamingApplication(){
        clients = new ArrayList<>();
        this.tcpClient = new TCPClient(1500);
    }
    
    public void connect(){
        tcpClient.addReceiver(this);
        tcpClient.start();
    }
    
    public static RemoteStreamingApplication getInstance(){
        if(instance == null){
            instance = new RemoteStreamingApplication();
        }
        return instance;
    }
    
    public void addListener(IStreamingClient client){
        clients.add(client);
    }

    @Override
    public void onNewFrame(byte[] bytes) {
        for(IStreamingClient client : clients){
            client.onNewFrame(bytes);
        }
    }

    @Override
    public void setScreenSize(Dimension dimension) {
        for(IStreamingClient client : clients){
            client.setScreenSize(dimension);
        }
    }
}
