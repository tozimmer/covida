/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2;

import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import de.dfki.covida.covidacore.tw.TouchAndWriteSupport;
import de.dfki.covida.visualjme2.components.CovidaJMEComponent;
import de.dfki.covida.visualjme2.components.JMENodeHandler;
import de.dfki.touchandwrite.TouchAndWriteDevice;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author tzimmermann
 */
public class ApplicationImpl extends AbstractApplication {

    /**
     * {@link TouchAndWriteDevice}
     */
    private final TouchAndWriteDevice device;
    private final JMENodeHandler nodeHandler;
    private List<Spatial> toCheck;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(ApplicationImpl.class);
    private List<Controller> toRemove;

    /**
     * Creates an instance of {@link ApplicationImpl}
     *
     * @param device {@link TouchAndWriteDevice}
     * @param windowtitle window title for the application
     */
    public ApplicationImpl(TouchAndWriteDevice device, String windowtitle) {
        super();
        this.device = device;
        this.windowtitle = windowtitle;
        this.nodeHandler = JMENodeHandler.getInstance();
        toCheck = new ArrayList<>();
    }

    /**
     * Loading animation
     *
     * Note that method have to be overridden for a loading animation.
     */
    protected void loadingAnimation() {
    }

    @Override
    protected void simpleInitGame() {
        TouchAndWriteSupport.start(device, this);
        loadingAnimation();
    }

    @Override
    public void setBackground() {
    }

    @Override
    public String getWindowTitle() {
        return windowtitle;
    }

    @Override
    public void executeRequests() {
        for (Spatial spatial : toCheck) {
            toRemove = new ArrayList<>();
            for (Controller controller : spatial.getControllers()) {
                if (!controller.isActive()) {
                    toRemove.add(controller);
                }
            }
            for (Controller controller : toRemove) {
                if (spatial instanceof CovidaJMEComponent) {
                    ((CovidaJMEComponent) spatial).executeRemoveController(controller);
                } else {
                    spatial.removeController(controller);
                }
            }
        }
        toCheck.clear();
        Map<Node, List<Spatial>> detachChildRequests = nodeHandler.getDetachChildRequests();
        for (Node node : detachChildRequests.keySet()) {
            for (Spatial spatial : detachChildRequests.get(node)) {
                if (node instanceof CovidaJMEComponent) {
                    ((CovidaJMEComponent) node).executeDetachChild(spatial);
                } else {
                    node.detachChild(spatial);
                }
            }
        }
        Map<Node, List<Spatial>> attachChildRequests = nodeHandler.getAttachChildRequests();
        for (Node node : attachChildRequests.keySet()) {
            for (Spatial spatial : attachChildRequests.get(node)) {
                if (node instanceof CovidaJMEComponent) {
                    ((CovidaJMEComponent) node).executeAttachChild(spatial);
                } else {
                    node.attachChild(spatial);
                }
            }
        }
        Map<Spatial, List<Controller>> removeControllerRequests = nodeHandler.getRemoveControllerRequests();
        for (Spatial spatial : removeControllerRequests.keySet()) {
            for (Controller controller : removeControllerRequests.get(spatial)) {
                if (spatial instanceof CovidaJMEComponent) {
                    ((CovidaJMEComponent) spatial).executeRemoveController(controller);
                } else {
                    spatial.removeController(controller);
                }
            }
        }
        Map<Spatial, List<Controller>> addControllerRequests = nodeHandler.getAddControllerRequests();
        for (Spatial spatial : addControllerRequests.keySet()) {
            for (Controller controller : addControllerRequests.get(spatial)) {
                toCheck.add(spatial);
                if (spatial instanceof CovidaJMEComponent) {
                    ((CovidaJMEComponent) spatial).executeAddController(controller);
                } else {
                    spatial.addController(controller);
                }
            }
        }
    }
}
