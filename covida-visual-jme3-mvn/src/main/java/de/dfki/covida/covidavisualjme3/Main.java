/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualjme3;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.sun.jna.Memory;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;

/**
 * A very basic example showing how to use a DirectMediaPlayer inside
 * JMonkeyEngine.
 * <p>
 * You MUST remember that the video update callback is executing asynchronously
 * on a native thread - you do not want to render the texture while it is being
 * updated so you should synchronise, somehow.
 * <p>
 * I am not a JMonkeyEngine expert, nor am I an OpenGL expert, so you must
 * work those things out for yourself. The synchronisation that is implemented
 * in this example class may or may not be correct! There may also be much more
 * efficient ways to copy the video memory to the texture, I really have no
 * idea.
 * <p>
 * This example does not clean-up the media player or factory when you exit - a
 * real application would implement that.
 * <p>
 * The same rules apply here as for any other vlcj application - the JVM must be
 * able to find the libvlc native libraries and all of the vlc plugins. You are
 * supposed to set something like "-Djna.library.path=/home/linux/vlc" in your
 * run configuration JVM arguments. You can also do this in code. In fact there
 * are many ways to configure your application to find the libvlc native
 * libraries so check the vlcj examples or search the project WIKI.
 */
public class Main extends SimpleApplication implements RenderCallback {

    /**
     * Media Resource Locator of the video you want to play.
     * <p>
     * For example "/home/video/MyCoolGameIntro.mp4".
     */
    private static final String MRL = "../covida-res/videos/Collaborative Video Annotation.mp4";
    
    /**
     * Video attributes.
     * <p>
     * These attributes are passed to libvlc to configure the video playback
     * buffer.
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
     * Application entry point.
     * 
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    /**
     * Create a new application.
     */
    public Main() {
        // You can do this to locate the libvlc native libraries - there are
        // other ways too, but hard-coding here is the most convenient for testing
        System.setProperty("jna.library.path", "../covida-res/vlc");

        // Create the vlcj resources...
        //
        // If your GPU supports it, "--ffmpeg-hw" should give your video some
        // hardware acceleration
        mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show", "--quiet");
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

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    @Override
    public void start() {
        super.start();
        // Play video...
        mediaPlayer.playMedia(MRL);
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
}
