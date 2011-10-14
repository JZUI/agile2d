/*****************************************************************************
 * Copyright (C) 2011,         *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/

package agile2d.benchmark;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.*;

/**
 * <b>AgileCanvas</b>
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class G2DFrame extends JFrame implements Runnable {
	private double incrementor = 1.0;
	private double zFactor = 1.00;	
	private final static int WIN_W = 1200;
	private final static int WIN_H = 800;
	private Thread thread;
	private static Chrono chrono;
	private int frame_counter;
	private static Font[] allFonts;
	private final static int NB_FONTS=36;
	private final static int NB_REPETITIONS=1;
	private static Font[] someFonts = new Font[NB_FONTS];
	private static Graphics2D g2;
	private final static float INIT_FONT_SIZE = 6.0f;
	private final static float MAX_SCALE = 9.0f;
	private Image mImage;
	
	static{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		allFonts = ge.getAllFonts();
		for(int i=0; i<NB_FONTS; i++)
			someFonts[i] = allFonts[i].deriveFont(INIT_FONT_SIZE);
	}

	public void start() {
	        thread = new Thread(this);
        	thread.setPriority(Thread.MIN_PRIORITY);
	        thread.start();
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
                thread.sleep(10);
            } catch (InterruptedException e) { break; }
        }
        thread = null;
    }
    
    public long getFPS(Chrono ch, int nb_frames){   	
    	ch.stop();
    	long duration_sec = ch.getDuration()/1000; 
    	ch.start();
    	return (nb_frames/duration_sec);
    }
    
    public void reset(int w, int h) {
    }

    public void step() {
    		//Incrementor ]0, 2*PI[
    		incrementor += 0.025;
    		incrementor %= (2*Math.PI);
    		//zFactor ]1.0, MAX_SCALE[ 
    		zFactor = MAX_SCALE*(Math.sin(incrementor)+1.1);
    		//Gets the fps once per cycle (when the angle approaches "0")
    		if(incrementor<0.025){
    			System.out.println("FPS: "+this.getFPS(this.chrono, this.frame_counter));
    			frame_counter=0;
    			System.out.println("zFactor: "+zFactor);
    		}   		
    }

	public void doPaint(Graphics g) {
		// Paint sample primitives
    		g2 = (Graphics2D)g;		
		g2.setBackground(Color.WHITE);	
		g2.clearRect(0, 0, 1200, 800);   	
		drawBigText(WIN_W, WIN_H, zFactor, g2);
		frame_counter++;
		this.step();
    }

	public static void main(String[] args) {
	    // create an identifier named 'window' and
	    // apply it to a new BasicFrame object
	    // created using our constructor, above.
	    G2DFrame frame = new G2DFrame();

	    // Use the setSize method that our BasicFrame
	    // object inherited to make the frame
	    // 200 pixels wide and high.
	    frame.setSize(WIN_W, WIN_H);

	    // Make the window show on the screen.
	    frame.setVisible(true);   

	        chrono = new Chrono();
	        chrono.start();		
		frame.setVisible(true);
		frame.start();
		
	}

	// Sample display to test text rendering performance during zooming
	void drawBigText(int x, int y, double zoomFactor, Graphics2D g2_) {
		g2_.drawRect(10, 10, 200, 200);
		g2_.fillRect(100, 100, 200, 200);
		g2_.scale(zoomFactor, zoomFactor);
		for(int i=0; i<(NB_REPETITIONS*NB_FONTS); i++){
			g2_.setFont(someFonts[i%NB_FONTS]);
			g2_.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 2, ((i+1)*INIT_FONT_SIZE));
		}
	}

	private static final class Chrono{
	    private long begin, end;	    
	 
	    public Chrono(){
	    	begin = end = 0;
	    }
	    
	    public void start(){
	        begin = System.currentTimeMillis();
	    }
	 
	    public void stop(){	    	
	        end = System.currentTimeMillis();
	    }
	    public long getDuration() {
	        return end-begin;
	    }
	}

}
