import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;

public class Test
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                createAndShowGUI();
            }
        });
    }

    private static BufferedImage createDummyImage()
    {
        int w = 20;
        int h = 10;
        BufferedImage image =
                new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,w,h);
        g.setColor(Color.WHITE);
        //g.drawString("DEMO", 10, 20);
        g.dispose();
        return image;
    }

    //version test

    private static int MonitorWidth = 1366;
    private static int MonitorHeight = 768;
    private static double XOrigin = -8573920;
    private static double YOrigin = 4705040;
    private static double Interval = 80;
    private static double HorizontalSquares = 4;
    private static double VerticalSquares = 4;

    static ImageIcon mapIcon;

    private static void createAndShowGUI()
    {
        JFrame f = new JFrame();
        final Graph<String, Number> graph = getGraph();

        int ImageWidth = 0;
        int ImageHeight = 0;

        String imageLocation = "/basicMap.png";

        try {
            mapIcon = new ImageIcon(Test.class.getResource(imageLocation));

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


        final VisualizationViewer<String,Number> vv = new VisualizationViewer<String, Number>(layout,
                new Dimension(MonitorWidth, MonitorHeight));

        vv.getRenderContext().setEdgeShapeTransformer(
                new EdgeShape.Line());


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

        final BufferedImage image = createDummyImage();

        Number edge = graph.getEdges().iterator().next();

        final ImageAtEdgePainter<String, Number> imageAtEdgePainter =
                new ImageAtEdgePainter<String, Number>(vv, edge, image);

        Timer t = new Timer(20, new ActionListener()
        {
            long prevMillis = 0;
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (prevMillis == 0)
                {
                    prevMillis = System.currentTimeMillis();
                }
                long dtMs = System.currentTimeMillis() - prevMillis;
                double dt = dtMs / 1000.0;
                double phase = 0.5 + Math.sin(dt) * 0.5;
                imageAtEdgePainter.setImageLocation(phase);
                vv.repaint();
            }
        });
        t.start();

        vv.addPostRenderPaintable(imageAtEdgePainter);


        f.getContentPane().add(vv);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }

    static class ImageAtEdgePainter<V, E> implements VisualizationViewer.Paintable
    {
        private final VisualizationViewer<V, E> vv;
        private final E edge;
        private final BufferedImage image;
        private double imageLocation;

        ImageAtEdgePainter(
                VisualizationViewer<V, E> vv,
                E edge,
                BufferedImage image)
        {
            this.vv = vv;
            this.edge = edge;
            this.image = image;
        }

        public void setImageLocation(double imageLocation)
        {
            this.imageLocation = imageLocation;
        }

        @Override
        public void paint(Graphics gr)
        {
            Graphics2D g = (Graphics2D)gr;
            Shape shape = getTransformedEdgeShape(vv, vv.getGraphLayout(), edge);
            Point2D p = computePointAt(shape, 0.2, imageLocation);
            //g.setColor(Color.BLUE);
            //g.draw(shape);
            //System.out.println(p);
            gr.drawImage(image, (int)p.getX(), (int)p.getY(), null);
        }
        @Override
        public boolean useTransform()
        {
            return true;
        }

    }

    private static double computeLength(Shape shape, double flatness)
    {
        double length = 0;
        PathIterator pi = shape.getPathIterator(null, flatness);
        double[] coords = new double[6];
        double previous[] = new double[2];
        while (!pi.isDone())
        {
            int segment = pi.currentSegment(coords);
            switch (segment)
            {
                case PathIterator.SEG_MOVETO:
                    previous[0] = coords[0];
                    previous[1] = coords[1];
                    break;

                case PathIterator.SEG_LINETO:
                    double dx = previous[0]-coords[0];
                    double dy = previous[1]-coords[1];
                    length += Math.sqrt(dx*dx+dy*dy);
                    previous[0] = coords[0];
                    previous[1] = coords[1];
                    break;
            }
            pi.next();
        }
        return length;
    }

    public static Point2D computePointAt(
            Shape shape, double flatness, double alpha)
    {
        alpha = Math.min(1.0, Math.max(0.0, alpha));
        double totalLength = computeLength(shape, flatness);
        double targetLength = alpha * totalLength;
        double currentLength = 0;
        PathIterator pi = shape.getPathIterator(null, flatness);
        double[] coords = new double[6];
        double previous[] = new double[2];
        while (!pi.isDone())
        {
            int segment = pi.currentSegment(coords);
            switch (segment)
            {
                case PathIterator.SEG_MOVETO:
                    previous[0] = coords[0];
                    previous[1] = coords[1];
                    break;

                case PathIterator.SEG_LINETO:
                    double dx = previous[0]-coords[0];
                    double dy = previous[1]-coords[1];
                    double segmentLength = Math.sqrt(dx*dx+dy*dy);
                    double nextLength = currentLength + segmentLength;
                    if (nextLength >= targetLength)
                    {
                        double localAlpha =
                                (currentLength - targetLength) / segmentLength;
                        //System.out.println("current "+currentLength+" target "+targetLength+" seg "+segmentLength);
                        double x = previous[0] + localAlpha * dx;
                        double y = previous[1] + localAlpha * dy;
                        return new Point2D.Double(x,y);
                    }
                    previous[0] = coords[0];
                    previous[1] = coords[1];
                    currentLength = nextLength;
                    break;
            }
            pi.next();
        }
        return null;
    }

    // This method is take from JUNG ShapePickSupport.java
    private static <V, E>  Shape getTransformedEdgeShape(
            VisualizationViewer<V, E> vv, Layout<V, E> layout, E e) {
        Pair<V> pair = layout.getGraph().getEndpoints(e);
        V v1 = pair.getFirst();
        V v2 = pair.getSecond();
        boolean isLoop = v1.equals(v2);
        RenderContext<V, E> rc = vv.getRenderContext();
        MultiLayerTransformer multiLayerTransformer =
                rc.getMultiLayerTransformer();
        Point2D p1 = multiLayerTransformer.transform(
                Layer.LAYOUT, layout.transform(v1));
        Point2D p2 = multiLayerTransformer.transform(
                Layer.LAYOUT, layout.transform(v2));
        if(p1 == null || p2 == null)
            return null;
        float x1 = (float) p1.getX();
        float y1 = (float) p1.getY();
        float x2 = (float) p2.getX();
        float y2 = (float) p2.getY();
        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);
        Shape edgeShape =
                rc.getEdgeShapeTransformer().transform(
                        Context.<Graph<V,E>,E>getInstance(
                                vv.getGraphLayout().getGraph(),e));
        if(isLoop) {
            Shape s2 = rc.getVertexShapeTransformer().transform(v2);
            Rectangle2D s2Bounds = s2.getBounds2D();
            xform.scale(s2Bounds.getWidth(),s2Bounds.getHeight());
            xform.translate(0, -edgeShape.getBounds2D().getHeight()/2);
        } else {
            float dx = x2 - x1;
            float dy = y2 - y1;
            double theta = Math.atan2(dy,dx);
            xform.rotate(theta);
            float dist = (float) Math.sqrt(dx*dx + dy*dy);
            xform.scale(dist, 1.0f);
        }
        edgeShape = xform.createTransformedShape(edgeShape);
        return edgeShape;
    }


    static Map<String, String[]> map = new HashMap<String, String[]>();

    static List<String> nodeList;

    public static Graph<String, Number> getGraph()
    {
        Graph<String, Number> g = new DirectedSparseMultigraph<String, Number>();

        map.put("POINT1", new String[]{"4705190", "-8574039"});
        map.put("POINT2", new String[]{"4705190", "-8573817"});
        //map.put("POINT3", new String[]{"4704960", "-8574039"});
        //map.put("POINT4", new String[]{"4704960", "-8573817"});

        for (String node : map.keySet()) {
            g.addVertex(node);
        }

        nodeList = new ArrayList<String>(map.keySet());

        for (int i = 0; i < map.keySet().size() * 1.3; i++) {
            g.addEdge(Math.random(), randomNode(), randomNode(), EdgeType.DIRECTED);
        }

        return g;
    }

    private static String randomNode() {
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
            latitude *= d.height / (Interval * VerticalSquares);

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