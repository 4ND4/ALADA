/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@yakubu please no more changes to file.

/**
 * Shows a graph overlaid on a world map image.
 * Scaling of the graph also scales the image background.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class Simulation extends JApplet {

    /**
     * the graph
     */
    Graph<String, Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<String, Number> vv;

    Map<String, String[]> map = new HashMap<String, String[]>();
    List<String> nodeList;

    /**
     * create an instance of a simple graph with controls to
     * demo the zoom features.
     */

    int ImageWidth = 0;
    int ImageHeight = 0;

    //version test

    private int MonitorWidth = 1366;
    private int MonitorHeight = 768;
    private static double XOrigin = -8573920;
    private static double YOrigin = 4705040;
    private static double Interval = 80;
    private static double HorizontalSquares = 4;
    private static double VerticalSquares = 4;

    public Simulation() {
        setLayout(new BorderLayout());

        //map.put("CENTER", new String[] {"4705120", "-8573920"});

        /*  TRUE COORDINATES
        map.put("POINT1", new String[] {"4705189.59146681", "-8574039.5481531"});
        map.put("POINT2", new String[] {"4705190.14709181", "-8573817.8537779"});
        map.put("POINT3", new String[] {"4704961.78521658", "-8574038.9925281"});
        map.put("POINT4", new String[] {"4704960.11834158", "-8573817.2981529"});
        */

        //STRAIGHT LINE COORDINATES

        map.put("POINT1", new String[]{"4705190", "-8574039"});
        map.put("POINT2", new String[]{"4705190", "-8573817"});
        map.put("POINT3", new String[]{"4704960", "-8574039"});
        map.put("POINT4", new String[]{"4704960", "-8573817"});

        nodeList = new ArrayList<String>(map.keySet());

        // create a simple graph for the demo        
        graph = new DirectedSparseMultigraph<String, Number>();
        createVertices();
        //createEdges();

        ImageIcon mapIcon = null;
        String imageLocation = "/basicMap.png";

        try {
            mapIcon = new ImageIcon(getClass().getResource(imageLocation));

            ImageWidth = mapIcon.getIconWidth();
            ImageHeight = mapIcon.getIconHeight();

        } catch (Exception ex) {
            System.err.println("Can't load \"" + imageLocation + "\"");
        }

        final ImageIcon icon = mapIcon;

        Dimension layoutSize = new Dimension(ImageWidth, ImageHeight);

        Layout<String, Number> layout = new StaticLayout<String, Number>(graph,
                new ChainedTransformer<String, Point2D>(new Transformer[]{
                        new CityTransformer(map),
                        new LatLonPixelTransformer(new Dimension(ImageWidth, ImageHeight))
                }));

        layout.setSize(layoutSize);
        vv = new VisualizationViewer<String, Number>(layout,
                new Dimension(MonitorWidth, MonitorHeight));

        if (icon != null) {
            vv.addPreRenderPaintable(new VisualizationViewer.Paintable() {
                public void paint(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    AffineTransform oldXform = g2d.getTransform();
                    AffineTransform lat =
                            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getTransform();
                    AffineTransform vat =
                            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).getTransform();
                    AffineTransform at = new AffineTransform();
                    at.concatenate(g2d.getTransform());
                    at.concatenate(vat);
                    at.concatenate(lat);
                    g2d.setTransform(at);
                    g.drawImage(icon.getImage(), 0, 0,
                            icon.getIconWidth(), icon.getIconHeight(), vv);
                    g2d.setTransform(oldXform);
                }

                public boolean useTransform() {
                    return false;
                }
            });
        }

        vv.getRenderContext().setEdgeShapeTransformer(
                new EdgeShape.Line());

        vv.getRenderer().setVertexRenderer(
                new GradientVertexRenderer<String, Number>(
                        Color.white, Color.red,
                        Color.white, Color.blue,
                        vv.getPickedVertexState(),
                        false));


        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
        vv.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);

        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        add(panel);
        //final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Object, Object>();
        //vv.setGraphMouse(graphMouse);

        final ScalingControl scaler = new CrossoverScalingControl();

        vv.scaleToLayout(scaler);

    }

    /**
     * create some vertices
     *
     * @return the Vertices in an array
     */
    private void createVertices() {
        for (String node : map.keySet()) {
            graph.addVertex(node);
        }
    }

    /**
     * create edges for this demo graph
     */




    void createEdges() {

        for (int i = 0; i < map.keySet().size() * 1.3; i++) {
            graph.addEdge(new Double(Math.random()), randomNode(), randomNode(), EdgeType.DIRECTED);
        }
    }

    private String randomNode() {
        int m = nodeList.size();
        return nodeList.get((int) (Math.random() * m));
    }


    static class CityTransformer implements Transformer<String, String[]> {

        Map<String, String[]> map;

        public CityTransformer(Map<String, String[]> map) {
            this.map = map;
        }

        /**
         * transform airport code to latlon string
         */
        public String[] transform(String city) {
            return map.get(city);
        }
    }

    static class LatLonPixelTransformer implements Transformer<String[], Point2D> {
        Dimension d;
        int startOffset;

        public LatLonPixelTransformer(Dimension d) {
            this.d = d;
        }

        /**
         * transform a lat
         */

        public Point2D transform(String[] latlon) {

            double latitude = 0;
            double longitude = 0;

            latitude = Double.parseDouble(latlon[0]);
            latitude = Math.abs(YOrigin) - Math.abs(latitude);
            latitude *= (d.height / (Interval * VerticalSquares));

            longitude = Double.parseDouble(latlon[1]);
            longitude = Math.abs(XOrigin) - Math.abs(longitude);
            longitude *= (d.width / (Interval * HorizontalSquares));

            if (latitude > 0) {
                latitude = d.height / 2 + latitude; //change sa
            } else {
                latitude = d.height / 2 + latitude;     //change sa
            }

            if (longitude > 0) {
                longitude = d.width / 2 + longitude;
            } else {
                longitude = d.width / 2 + longitude;    //change sa
            }

            return new Point2D.Double(longitude, latitude);
        }
    }
}