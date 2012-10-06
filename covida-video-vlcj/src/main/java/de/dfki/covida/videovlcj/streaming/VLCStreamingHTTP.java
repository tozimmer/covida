/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.videovlcj.streaming;

import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

/**
 *
 * @author Tobias
 */
public class VLCStreamingHTTP implements Runnable{
    
    /**
     * Logger.
     */
    private Logger log = Logger.getLogger(VLCStreamingHTTP.class);
    private final String media;
    private final String options;
    
    public VLCStreamingHTTP(String mrl) {
        this.media = mrl;
        this.options = formatHttpStream("127.0.0.1", 5555);
    }

    private static String formatHttpStream(String serverAddress, int serverPort) {
        StringBuilder sb = new StringBuilder(60);
        sb.append(":sout=#duplicate{dst=std{access=http,mux=ts,");
        sb.append("dst=");
        sb.append(serverAddress);
        sb.append(':');
        sb.append(serverPort);
        sb.append("}}");
        return sb.toString();
    }

    @Override
    public void run() {
        log.debug("Streaming '" + media + "' to '" + options + "'");

        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(media);
        HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
        mediaPlayer.playMedia(media, options);
        try {
            // Don't exit
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            log.debug(ex);
        }
    }
    
}
