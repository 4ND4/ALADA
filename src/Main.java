import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        JFrame frame1 = new JFrame("Layout");


        //Container content = frame.getContentPane();
        //content.add(new Simulation());

        frame1.add(new Simulation());


        frame1.pack();
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame1.setVisible(true);

        JFrame frame2 = new JFrame("Train Demo");
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setSize(800, 400);
        frame2.setLocationRelativeTo(null);
        frame2.add(new TrainCanvas());
        frame2.setVisible(true);

    }
}

/*
JFrame frame = new JFrame("Train Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);
        frame.add(new TrainCanvas());
        frame.setVisible(true);
 */