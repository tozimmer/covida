/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualjme3;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.sun.jna.Memory;
import de.dfki.covida.covidacore.tw.IApplication;
import org.apache.log4j.Logger;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;

/**
 *
 * @author Tobias
 */
public class MainApplication extends AbstractApplication implements IApplication, RenderCallback {

    /**
     * Media Resource Locator of the video you want to play. <p> For example
     * "/home/video/MyCoolGameIntro.mp4".
     */
    private static final String MRL = "../covida-res/videos/Collaborative Video Annotation.mp4";
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(MainApplication.class);
    /**
     * Video attributes. <p> These attributes are passed to libvlc to configure
     * the video playback buffer.
     */
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int DEPTH = 4;
    private static final String VIDEO_FORMAT = "RGBA";
    /**
     * Texture format.
     */
    private static final Format TEXTURE_FORMAT = Format.RGBA16;
    /**
     * vlcj media player factory.
     */
    private final MediaPlayerFactory mediaPlayerFactory;
    /**
     * vlcj media player.
     */
    private final DirectMediaPlayer mediaPlayer;
    /**
     * Video texture.
     */
    private final Image videoImage;
    private final Texture2D videoTexture;

    /**
     * Create a new application.
     */
    public MainApplication(String windowtitle) {
        AppSettings setting = new AppSettings(true);
        setting.setTitle(windowtitle); //instead of child of ace you should write the window title you want
        setShowSettings(false);
        setSettings(setting);
        // Create the vlcj resources...
        //
        // If your GPU supports it, "--ffmpeg-hw" should give your video some
        // hardware acceleration
        mediaPlayerFactory = new MediaPlayerFactory("--ffmpeg-hw", "--no-video-title-show", "--quiet");
        mediaPlayer = mediaPlayerFactory.newDirectMediaPlayer(VIDEO_FORMAT, WIDTH, HEIGHT, WIDTH * DEPTH, this);

        // Create the texture for the video
        videoImage = new Image(TEXTURE_FORMAT, WIDTH, HEIGHT, null);
        videoTexture = new Texture2D(videoImage);
    }
    
    @Override
    public void simpleInitApp() {
        Box videoBox = new Box(new Vector3f(-3f, 1.1f, 0f), 2f, 1f, 2f);
        Geometry cube = new Geometry("My Video Box", videoBox);
        Material mat_stl = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_stl.setTexture("ColorMap", videoTexture);
        cube.setMaterial(mat_stl);
        rootNode.attachChild(cube);
    }

//    @Override
//    public void simpleInitApp() {
//        Box videoBox = new Box(new Vector3f(-3f, 1.1f, 0f), 2f, 1f, 2f);
//        Geometry cube = new Geometry("My Video Box", videoBox);
//        Material mat_stl = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat_stl.setTexture("ColorMap", videoTexture);
//        cube.setMaterial(mat_stl);
//        rootNode.attachChild(cube);
//    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    @Override
    public void start() {
        super.start();
        log.debug("Start video... ");
        mediaPlayer.playMedia(MRL);
        mediaPlayer.start();
        
    }

    @Override
    public void display(Memory memory) {
        // BEWARE!
        //
        //  Synchronisation might be required...

        // Copy the native memory to the video image
        videoImage.setData(memory.getByteBuffer(0, WIDTH * HEIGHT * DEPTH));
        // Set the new image on the texture
        videoTexture.setImage(videoImage);
    }
//    
//    private Texture2D videoTexture;
//    private RenderedVideoHandler video;
//    /**
//     * Video attributes.
//     * <p>
//     * These attributes are passed to libvlc to configure the video playback
//     * buffer.
//     */
//    private static final int WIDTH = 320;
//    private static final int HEIGHT = 240;
//    private static final int DEPTH = 4;
//    private static final String VIDEO_FORMAT = "RGBA";
//    /**
//     * Media Resource Locator of the video you want to play.
//     * <p>
//     * For example "/home/video/MyCoolGameIntro.mp4".
//     */
//    private static final String MRL = "../covida-res/videos/Collaborative Video Annotation.mp4";
//    
//    /**
//     * Texture format.
//     */
//    private static final Format TEXTURE_FORMAT = Format.RGBA16;
//    private Image videoImage;
//    private MediaPlayerFactory mediaPlayerFactory;
//    private DirectMediaPlayer mediaPlayer;
//
//    public MainApplication(String windowtitle) {
//        AppSettings setting = new AppSettings(true);
//        setting.setTitle(windowtitle); //instead of child of ace you should write the window title you want
//        setShowSettings(false);
//        setSettings(setting);
//    }
//

    @Override
    public String getWindowTitle() {
        return settings.getTitle();
    }
//
//    @Override
//    public void simpleInitApp() {
//         // Create the vlcj resources...
//        //
//        // If your GPU supports it, "--ffmpeg-hw" should give your video some
//        // hardware acceleration
//        mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show", "--quiet");
//        mediaPlayer = mediaPlayerFactory.newDirectMediaPlayer(VIDEO_FORMAT, WIDTH, HEIGHT, WIDTH * DEPTH, this);
//
//        // Create the texture for the video
//        videoImage = new Image(TEXTURE_FORMAT, WIDTH, HEIGHT, null);
//        videoTexture = new Texture2D(videoImage);
//        // create a simple plane/quad
//        Quad backgroundQuad = new Quad(1, 1);
//        backgroundQuad.updateGeometry(1, 1, false);
//        backgroundQuad.updateBound();
//        Geometry background = new Geometry("Background", backgroundQuad);
//        Texture backgroundTex = assetManager.loadTexture("background.png");
//        backgroundTex.setMinFilter(Texture.MinFilter.Trilinear);
//        Material backgroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        backgroundMat.setTexture("ColorMap", videoTexture);
//        background.setMaterial(backgroundMat);
//        background.setLocalTranslation(-0.5f, -0.5f, 0);
//        rootNode.attachChild(background);
//        Rectangle bounds = new Rectangle(0, 0, cam.getWidth(), cam.getHeight());
//        guiNode.attachChild(JME3Utils.createCenteredText(assetManager, "COVIDA",
//                0, cam.getHeight(), bounds));
//        mediaPlayer.playMedia(MRL);
//    }
//    
//    @Override
//    public void display(Memory memory) {
//        // BEWARE!
//        //
//        //  Synchronisation might be required...
//        
//        // Copy the native memory to the video image
//        videoImage.setData(memory.getByteBuffer(0, WIDTH * HEIGHT * DEPTH));
//        // Set the new image on the texture
//        videoTexture.setImage(videoImage);
//    }
}
