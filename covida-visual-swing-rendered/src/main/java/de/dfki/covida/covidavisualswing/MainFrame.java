/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualswing;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 *
 * @author Tobias
 */
public class MainFrame {


    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        VideoFrame frame = new VideoFrame("vlcj Tutorial");
    }

    public static void main(String[] args) {
        NativeLibrary.addSearchPath(
                RuntimeUtil.getLibVlcLibraryName(), "../vlc");
        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mainFrame = new MainFrame();
            }
        });
    }
}
