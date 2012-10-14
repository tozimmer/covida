/*
 * ClientDemo.java
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
package de.dfki.covida.covidaflvcreator.demos;

import de.dfki.covida.covidaflvcreator.client.TCPClient;
import de.dfki.covida.covidaflvcreator.utils.CreationRequest;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Demonstration of the usage of the tcp client.
 *
 * @author Tobias Zimmermann <Tobias.Zimmermann@dfki.de>
 */
public class ClientDemo {

    /**
     * Logger
     */
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ClientDemo.class);
    
    /**
     * Starts a TCP Client.
     * 
     * @param args first argument will be interpreted as socket port number e.g.
     * argument 2345 will set the socket port number to 2345
     */
    public static void main(String[] args) {
        int port;
        if(args.length > 0){
            try{
            port = Integer.parseInt(args[0]);
            } catch(NumberFormatException e){
                log.error("",e);
                port = 1500;
            }
        }else{
            port = 1500;
        }
        TCPClient client = new TCPClientImplDemo(port);
        Thread clientThread = new Thread(client);
        clientThread.setName("Client Thread");
        clientThread.start();

        List<Point> shape = new ArrayList<>();
        shape.add(new Point(22, 177));
        shape.add(new Point(200, 22));
        shape.add(new Point(155, 43));
        shape.add(new Point(210, 77));
        shape.add(new Point(244, 17));
        shape.add(new Point(22, 7));
        shape.add(new Point(331, 177));
        shape.add(new Point(321, 173));
        shape.add(new Point(100, 177));
        shape.add(new Point(22, 177));

        String filename = "../covida-res/videos/Collaborative Video Annotation.mp4";

        long timeStart = 20000000;
        long timeEnd = 25000000;

        String label = "Test label";

        while (true) {
            timeStart++;
            timeEnd++;
            CreationRequest request = new CreationRequest(filename, timeStart,
                    timeEnd, shape, label);
            client.writeRequest(request);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                log.error("",ex);
                break;
            }
        }
    }
}
