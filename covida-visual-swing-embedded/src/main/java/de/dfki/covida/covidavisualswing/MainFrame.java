/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualswing;

import de.dfki.covida.covidacore.components.IControlableComponent;
import de.dfki.covida.covidacore.tw.IApplication;
import de.dfki.covida.covidacore.utils.ActionName;
import de.dfki.covida.covidavisualswing.controls.ControlButton;
import de.dfki.covida.videovlcj.embedded.EmbeddedVideoHandler;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import de.dfki.covida.covidavisualswing.overlay.SwingDisplayWindow;

/**
 *
 * @author Tobias Zimmermann
 */
public class MainFrame extends JFrame implements IControlableComponent, IApplication {

    private Window overlayWindow;
    private boolean handlingErrors;
    private final EmbeddedVideoHandler video;
    private Map<ActionName, ControlButton> controls;
    private SwingDisplayWindow monitorDisplay;
    private Dimension screenSize;

    /**
     * Creates new form MainFrame
     */
    public MainFrame(String Windowtitle) {
        initComponents();
        setTitle(Windowtitle);
//        JButton button = new JButton("Test Test Test Teest Test Test");
        Canvas c = new Canvas();
//        VideoField panelWest = new VideoField();
//        button.setSize(350, 1000);
//        panelWest.add(button);
//        VideoField panelEast = new VideoField();
//        panelEast.add(button);
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JPanel control_panel = new JPanel();
        control_panel.setLayout(new GridLayout(1, 5));
//        control_panel.add(panel, BorderLayout.CENTER);
//        panel.setBorder(new EmptyBorder(new Insets(40, 60, 40, 60)));
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(c, BorderLayout.CENTER);
        getContentPane().add(control_panel, BorderLayout.SOUTH);
//        getContentPane().add(panelEast, BorderLayout.EAST);
//        getContentPane().add(panelWest, BorderLayout.WEST);
        c.setBackground(Color.black);
        video = new EmbeddedVideoHandler("../covida-res/videos/Collaborative Video Annotation.mp4", "Covida Demo", c, 800, 450);
        ClassLoader loader = getClass().getClassLoader();
        List<ActionName> actions = new ArrayList<>();
        actions.add(ActionName.BACKWARD);
        actions.add(ActionName.STOP);
        actions.add(ActionName.PLAYPAUSE);
        actions.add(ActionName.FORWARD);
        actions.add(ActionName.SOUND);
        actions.add(ActionName.SAVE);
        actions.add(ActionName.DELETE);
        Map<ActionName, URL> urls = new HashMap<>();
        urls.put(ActionName.BACKWARD, loader.getResource("ui/video_controls_back.png"));
        urls.put(ActionName.STOP, loader.getResource("ui/video_controls_stop.png"));
        urls.put(ActionName.PLAYPAUSE, loader.getResource("ui/video_controls_play.png"));
        urls.put(ActionName.FORWARD, loader.getResource("ui/video_controls_forward.png"));
        urls.put(ActionName.SOUND, loader.getResource("ui/video_controls_soundoff.png"));
        urls.put(ActionName.SAVE, loader.getResource("ui/video_controls_save.png"));
        urls.put(ActionName.DELETE, loader.getResource("ui/video_controls_delete.png"));
        Map<ActionName, URL> selectedURLs = new HashMap<>();
        selectedURLs.put(ActionName.BACKWARD, loader.getResource("ui/video_controls_back.png"));
        selectedURLs.put(ActionName.STOP, loader.getResource("ui/video_controls_stop.png"));
        selectedURLs.put(ActionName.PLAYPAUSE, loader.getResource("ui/video_controls_pause.png"));
        selectedURLs.put(ActionName.FORWARD, loader.getResource("ui/video_controls_forward.png"));
        selectedURLs.put(ActionName.SOUND, loader.getResource("ui/video_controls_sound.png"));
        selectedURLs.put(ActionName.SAVE, loader.getResource("ui/video_controls_save.png"));
        selectedURLs.put(ActionName.DELETE, loader.getResource("ui/video_controls_delete.png"));
        controls = new HashMap<>();
        for (ActionName action : actions) {
            ControlButton b;
            b = new ControlButton(urls.get(action), selectedURLs.get(action),
                    action, this, 64, 64);
            control_panel.add(b);
            controls.put(action, b);
        }
        pack();
    }

    public void handleErrors() {
        if (!handlingErrors) {
            addWindowListener(new VlcjBackgroundFrameWindowAdapter());
            handlingErrors = true;
        }
    }

    public void setOverlay(JWindow win) {
//        video.setOverlay(win);
        overlayWindow = video.getOverlay();
    }

    /**
     * Exit the Application
     */
    private void exitForm(java.awt.event.WindowEvent evt) {
        cleanup();
        System.exit(0);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        overlayWindow = video.getOverlay();
        video.enableOverlay(b);
        video.start();
        controls.get(ActionName.PLAYPAUSE).setSelected(true);
//        VLCStreamingHTTP streaming = new VLCStreamingHTTP("screen://");
//        Thread streaminThread = new Thread(streaming);
//        streaminThread.start();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
//        GroupLayout layout = new GroupLayout(getContentPane());
//        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGap(0, 400, Short.MAX_VALUE));
//        layout.setVerticalGroup(
//                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//                .addGap(0, 300, Short.MAX_VALUE));

    }

    @Override
    public boolean toggle(ActionName action) {
        if (action.equals(ActionName.BACKWARD)) {
            if ((video.getTime() - video.getMaxTime() / 20) > 0) {
                video.setTimePosition(video.getTime()
                        - video.getMaxTime() / 20);
            } else {
                video.setTimePosition(0);
            }
            return false;
        } else if (action.equals(ActionName.CHANGEMEDIA)) {

            return false;
        } else if (action.equals(ActionName.CLOSE)) {
            System.exit(1);
            return false;
        } else if (action.equals(ActionName.FORWARD)) {
            if ((video.getTime() + video.getMaxTime() / 100) < video.getMaxTime()) {
                video.setTimePosition(video.getTime()
                        + video.getMaxTime() / 100);
            } else {
                video.setTimePosition(video.getMaxTime());
            }
            return false;
        } else if (action.equals(ActionName.LIST)) {
            return false;
        } else if (action.equals(ActionName.PLAYPAUSE)) {
            if (video.isPlaying()) {
                video.pause();
                return true;
            } else {
                video.resume();
                controls.get(ActionName.STOP).setSelected(false);
                return false;
            }
        } else if (action.equals(ActionName.STOP)) {
            if (video.isPlaying()) {
                video.stop();
                controls.get(ActionName.PLAYPAUSE).setSelected(false);
                return true;
            }
        } else if (action.equals(ActionName.SOUND)) {
            if (video.getVolume() > 0) {
                video.setVolume(0);
                return false;
            } else {
                video.setVolume(100);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getId() {
        return 0;
    }

    void cleanup() {
        video.cleanup();
    }

    @Override
    public void start() {
        monitorDisplay = new SwingDisplayWindow();
        screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setVisible(true);
        monitorDisplay.setSize(screenSize);
        monitorDisplay.setVisible(true);
        setOverlay(monitorDisplay);
    }

    @Override
    public String getWindowTitle() {
        return getTitle();
    }

    private class VlcjBackgroundFrameWindowAdapter extends WindowAdapter {

        /**
         * Invoked when a window has been opened.
         */
        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(java.awt.event.WindowEvent evt) {
            exitForm(evt);
        }
        
        /**
         * Invoked when a window has been closed.
         */
        @Override
        public void windowClosed(WindowEvent e) {
        }

        /**
         * Invoked when a window is iconified.
         */
        @Override
        public void windowIconified(WindowEvent e) {
        }

        /**
         * Invoked when a window is de-iconified.
         */
        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        /**
         * Invoked when a window is activated.
         */
        @Override
        public void windowActivated(WindowEvent e) {
        }

        /**
         * Invoked when a window is de-activated.
         */
        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        /**
         * Invoked when a window state is changed.
         *
         * @since 1.4
         */
        @Override
        public void windowStateChanged(WindowEvent e) {
            overlayWindow.toFront();
        }

        /**
         * Invoked when the Window is set to be the focused Window, which means
         * that the Window, or one of its subcomponents, will receive keyboard
         * events.
         *
         * @since 1.4
         */
        @Override
        public void windowGainedFocus(WindowEvent e) {
        }

        /**
         * Invoked when the Window is no longer the focused Window, which means
         * that keyboard events will no longer be delivered to the Window or any
         * of its subcomponents.
         *
         * @since 1.4
         */
        @Override
        public void windowLostFocus(WindowEvent e) {
        }
    }
}