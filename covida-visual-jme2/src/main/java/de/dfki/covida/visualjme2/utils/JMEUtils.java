/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.utils;

import com.jme.scene.state.BlendState;
import com.jme.system.DisplaySystem;
import java.awt.AlphaComposite;

/**
 *
 * @author Tobias
 */
public class JMEUtils {

    private static BlendState alpha = null;
    public static final AlphaComposite TRANSPARENT = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.0f);
    public static final AlphaComposite SOLID = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 1.0f);

    public static BlendState initalizeBlendState() {
        if (alpha == null) {
            alpha = DisplaySystem.getDisplaySystem().getRenderer().createBlendState();
            alpha.setEnabled(true);
            alpha.setBlendEnabled(true);

            alpha.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            alpha.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            alpha.setTestEnabled(true);
            alpha.setTestFunction(BlendState.TestFunction.GreaterThan);
        }
        return alpha;
    }
}
