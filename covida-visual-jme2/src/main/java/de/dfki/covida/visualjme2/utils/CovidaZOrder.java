/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.utils;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tobias
 */
public class CovidaZOrder {

    /**
     * Instance of {@link FontLoader}
     */
    private static CovidaZOrder instance;
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(FontLoader.class);

    public static Logger getLog() {
        return log;
    }
    private int background;
    private int ui_background;
    private int ui_node;
    private int ui_overlay;
    private int ui_text;
    private int ui_button;
    private int preload;
    private int ui_cornermenus;

    /**
     * Private constructor of {@link FontLoader}
     */
    private CovidaZOrder() {
        background = 10000;
        ui_background = 9000;
        ui_node = 8000;
        ui_overlay = 7000;
        ui_text = 6000;
        ui_button = 5000;
        preload = 1000;
        ui_cornermenus = 4000;
    }

    public synchronized static CovidaZOrder getInstance() {
        if (instance == null) {
            instance = new CovidaZOrder();
        }
        return instance;
    }

    public int getBackground() {
        background -= 10;
        return background;
    }

    public int getUi_background() {
        ui_background -= 10;
        return ui_background;
    }

    public int getUi_node() {
        ui_node -= 10;
        return ui_node;
    }

    public int getUi_overlay() {
        ui_overlay -= 10;
        return ui_overlay;
    }

    public int getUi_text() {
        ui_text -= 10;
        return ui_text;
    }

    public int getUi_button() {
        ui_button -= 10;
        return ui_button;
    }

    public int getPreload() {
        preload -= 10;
        return preload;
    }

    public int getUi_cornermenus() {
        ui_cornermenus -= 10;
        return ui_cornermenus;
    }
}
