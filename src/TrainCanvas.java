import javax.swing.*;
import java.awt.*;

/**
 * Created by santiago on 07/02/16.
 */
public class TrainCanvas extends JComponent {

    private int lastX = 0;

    public TrainCanvas() {
        Thread animationThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    repaint();
                    try {Thread.sleep(10);} catch (Exception ex) {}
                }
            }
        });

        animationThread.start();
    }

    public void paintComponent(Graphics g) {
        Graphics2D gg = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        int trainW = 100;
        int trainH = 10;
        int trainSpeed = 2;

        int x = lastX + trainSpeed;

        if (x > w + trainW) {
            x = -trainW;
        }

        gg.setColor(Color.BLACK);
        gg.fillRect(x, h/2 + trainH, trainW, trainH);

        lastX = x;
    }
}