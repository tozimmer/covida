/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.covidavisualjme3.utils;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import jme3tools.converters.MipMapGenerator;

/**
 *
 * @author Tobias
 */
public class JME3Utils {

    public static Geometry createGeometry(AssetManager assetManager, Quad sourceQuad,
            Texture sourceTexture) {
        Geometry geometry = new Geometry("Textured Quad 2", sourceQuad);
        Texture texCustomMip = sourceTexture.clone();
        Image imageCustomMip = texCustomMip.getImage().clone();
        MipMapGenerator.generateMipMaps(imageCustomMip);
        texCustomMip.setImage(imageCustomMip);
        Material mat2 = new Material(assetManager, 
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setTexture("ColorMap", texCustomMip);
        geometry.setMaterial(mat2);
        geometry.setLocalTranslation(-0.5f, -0.5f, 0);
        return geometry;
    }
    
    public static BitmapText createText(AssetManager assetManager, String text,
            float x, float y){
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText txt = new BitmapText(guiFont, false);
        txt.setBox(new Rectangle(0, 0, 96, 256));
        txt.setText(text);
        txt.setLocalTranslation(x, y, 0);
        return txt;
    }
    
    public static BitmapText createCenteredText(AssetManager assetManager, String text,
            float x, float y, Rectangle bounds){
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText txt = new BitmapText(guiFont, false);
        txt.setBox(bounds);
        txt.setAlignment(BitmapFont.Align.Center);
        txt.setText(text);
        txt.setLocalTranslation(x, y, 0);
        return txt;
    }
}
