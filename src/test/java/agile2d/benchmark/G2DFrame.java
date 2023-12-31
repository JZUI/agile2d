/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d.benchmark;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Image;
import javax.swing.*;

/**
 * <b>AgileCanvas</b>
 *
 */
public class G2DFrame extends JPanel implements Runnable {
    private static final long serialVersionUID = 9044699885885527920L;
    private static Graphics2D g2;
    private static Chrono chrono;
    private Thread thread;
    private Image mImage;
    private AnimeBenchmark bench;

    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public G2DFrame(){
        chrono = new Chrono();
        bench = new AnimeBenchmark(chrono);
        bench.resetCounter();
    }

    public void paint(Graphics g) {
        // Clear the offscreen image.
        Dimension d = getSize();
        checkOffscreenImage();
        Graphics offG = mImage.getGraphics();
        offG.setColor(getBackground());
        offG.fillRect(0, 0, d.width, d.height);
        // Draw into the offscreen image.
        paintOffscreen(mImage.getGraphics());
        // Put the offscreen image on the screen.
        g.drawImage(mImage, 0, 0, null);
    }

    private void checkOffscreenImage() {
        Dimension d = getSize();
        if (mImage == null || mImage.getWidth(null) != d.width
                || mImage.getHeight(null) != d.height) {
            mImage = createImage(d.width, d.height);
        }
    }
    public void paintOffscreen(Graphics g) {
        doPaint(g);
    }

    public synchronized void stop() {
        thread = null;
    }


    public void run() {
        Thread me = Thread.currentThread();
        while (thread == me) {
            super.repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) { break; }
        }
        thread = null;
    }

    public void reset(int w, int h) {
    }

    public void doPaint(Graphics g) {
        // Paint sample primitives
        g2 = (Graphics2D)g;
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, AnimeBenchmark.WIN_W, AnimeBenchmark.WIN_H);
        AnimeBenchmark.drawFullOvals(g2);
        AnimeBenchmark.drawRects(g2);
        AnimeBenchmark.drawImages(g2);
        AnimeBenchmark.drawBigText(AnimeBenchmark.WIN_W, AnimeBenchmark.WIN_H, g2);
        AnimeBenchmark.drawEmptyOvals(g2);
        bench.increment();
        bench.step();
    }

    public AnimeBenchmark getRefToBench(){
        if(bench==null)
            System.out.println("Warning. Bench is empty");
        return bench;
    }
}
