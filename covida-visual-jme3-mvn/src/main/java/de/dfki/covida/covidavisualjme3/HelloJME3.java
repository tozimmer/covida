package de.dfki.covida.covidavisualjme3;
 
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
 
/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class HelloJME3 extends SimpleApplication {
 
    public static void main(String[] args){
        HelloJME3 app = new HelloJME3();
        app.start(); // start the game
    }
 
    @Override
    public void simpleInitApp() {
        Texture tex = assetManager.loadTexture("http://www.fashionbids.com/fashionbids/css/screen/images/logo.png");
//        Box b = new Box(Vector3f.ZERO, 1, 1, 1); // create cube shape at the origin
        Quad background = new Quad(1280, 800);
        Material mat = new Material();
        mat.setTexture("background", tex);
        Geometry geom = new Geometry("bg", background);
        geom.setMaterial(mat);                 // set the cube's material
        rootNode.attachChild(geom);              // make the cube appear in the scene
        cam.setParallelProjection(true);
        flyCam.setEnabled(false);
    }
}