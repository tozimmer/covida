/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.covida.visualjme2.components;

import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Tobias
 */
public class JMENodeHandler {

    private static JMENodeHandler instance;
    private Node rootNode;
    private Map<Node, List<Spatial>> attachChildRequests;
    private Map<Node, List<Spatial>> detachChildRequests;
    private Map<Spatial, List<Controller>> addControllerRequests;
    private Map<Spatial, List<Controller>> removeControllerRequests;
    /**
     * Logger
     */
    private Logger log = Logger.getLogger(JMENodeHandler.class);

    private JMENodeHandler() {
        attachChildRequests = Collections.synchronizedMap(new HashMap<Node, List<Spatial>>());
        detachChildRequests = Collections.synchronizedMap(new HashMap<Node, List<Spatial>>());
        addControllerRequests = Collections.synchronizedMap(new HashMap<Spatial, List<Controller>>());
        removeControllerRequests = Collections.synchronizedMap(new HashMap<Spatial, List<Controller>>());
    }

    public static JMENodeHandler getInstance() {
        if (instance == null) {
            instance = new JMENodeHandler();
        }
        return instance;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void addAttachChildRequest(Node node, Spatial spatial) {
        if (attachChildRequests.containsKey(node)) {
            attachChildRequests.get(node).add(spatial);
        } else {
            List<Spatial> spatials = new ArrayList<>();
            spatials.add(spatial);
            attachChildRequests.put(node, spatials);
        }
    }

    public void addDetachChildRequest(Node node, Spatial spatial) {
        if (detachChildRequests.containsKey(node)) {
            detachChildRequests.get(node).add(spatial);
        } else {
            List<Spatial> spatials = new ArrayList<>();
            spatials.add(spatial);
            detachChildRequests.put(node, spatials);
        }
    }

    public void addAddControllerRequest(Spatial spatial, Controller controller) {
        if (addControllerRequests.containsKey(spatial)) {
            addControllerRequests.get(spatial).add(controller);
        } else {
            List<Controller> controllers = new ArrayList<>();
            controllers.add(controller);
            addControllerRequests.put(spatial, controllers);
        }
    }

    public void addRemoveControllerRequest(Spatial spatial, Controller controller) {
        if (removeControllerRequests.containsKey(spatial)) {
            removeControllerRequests.get(spatial).add(controller);
        } else {
            List<Controller> controllers = new ArrayList<>();
            controllers.add(controller);
            removeControllerRequests.put(spatial, controllers);
        }
    }

    public Map<Node, List<Spatial>> getAttachChildRequests() {
        Map<Node, List<Spatial>> requests = Collections.synchronizedMap(new HashMap<Node, List<Spatial>>());
        for (Node node : attachChildRequests.keySet()) {
            requests.put(node, attachChildRequests.get(node));
        }
        attachChildRequests.clear();
        return requests;
    }

    public Map<Node, List<Spatial>> getDetachChildRequests() {
        Map<Node, List<Spatial>> requests = Collections.synchronizedMap(new HashMap<>(detachChildRequests));
        detachChildRequests.clear();
        return requests;
    }

    public Map<Spatial, List<Controller>> getAddControllerRequests() {
        Map<Spatial, List<Controller>> requests = Collections.synchronizedMap(new HashMap<>(addControllerRequests));
        addControllerRequests.clear();
        return requests;
    }

    public Map<Spatial, List<Controller>> getRemoveControllerRequests() {
        Map<Spatial, List<Controller>> requests = Collections.synchronizedMap(new HashMap<>(addControllerRequests));
        removeControllerRequests.clear();
        return requests;
    }
}
