/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;

import java.awt.Color;

import java.awt.Font;
import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.geom.*;
import java.awt.font.TextAttribute;
import java.awt.RenderingHints;
import java.awt.event.*;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;

import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

/**
 * <b>AgileCanvas</b>
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class AgileAnimeCanvas implements GLEventListener, KeyListener, Runnable {
	private AgileGraphics2D jgraphics;
	private Component root;
	private BufferedImage img_buff = null;
	private Image img = null;
	private int keyPressed, exampleNb;
	private boolean interactive_antialias = false;
	private static int NB_OF_SAMPLES_FOR_MULTISAMPLE = 4;
	private double rTheta = 1.0;
	private double zFactor = 1.00;	
	private final static int WIN_W = 1200;
	private final static int WIN_H = 800;
	private Thread thread;
	
	public AgileAnimeCanvas(Component root) {
		this.root = root;

	}

	public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }


    public synchronized void stop() {
        thread = null;
    }


    public void run() {
        Thread me = Thread.currentThread();
        while (thread == me) {
            root.repaint();
            try {
                thread.sleep(10);
            } catch (InterruptedException e) { break; }
        }
        thread = null;
    }
	
	
    public void reset(int w, int h) {
     //   size = (w > h) ? h/6f : w/6f;
     /*   for (int i = 0; i < lines.length; i++) {
            lines[i] = new Line2D.Float(0,0,size,0);
            strokes[i] = new BasicStroke(size/3, caps[i], joins[i]);
            rAmt[i] = i * 360/lines.length;
            direction[i] = i%2;
            speed[i] = i + 1;
        }
	*/
        /*
    	path = new GeneralPath();
        path.moveTo(size, -size/2);
        path.lineTo(size+size/2, 0);
        path.lineTo(size, +size/2);

        ellipse.setFrame(w/2-size*2-4.5f,h/2-size*2-4.5f,size*4,size*4);
        PathIterator pi = ellipse.getPathIterator(null, 0.9);
        Point2D[] points = new Point2D[100];
        int num_pts = 0;
        while ( !pi.isDone() ) {
            float[] pt = new float[6];
            switch ( pi.currentSegment(pt) ) {
                case FlatteningPathIterator.SEG_MOVETO:
                case FlatteningPathIterator.SEG_LINETO:
                    points[num_pts] = new Point2D.Float(pt[0], pt[1]);
                    num_pts++;
            }
            pi.next();
        }
        pts = new Point2D[num_pts];
        System.arraycopy(points, 0, pts, 0, num_pts);
        */
    }

    public void step() {
    		zFactor += 0.05;
    		System.out.println("zFactor: "+zFactor);
    }
	
	public void init(GLAutoDrawable drawable) {
		GLU glu = new GLU();
		jgraphics = new AgileGraphics2D(drawable);
		GL2 gl = drawable.getGL().getGL2();
		System.out.println("INIT GL IS: " + gl.getClass().getName());
		System.out.println("GLU version is: "
				+ glu.gluGetString(GLU.GLU_VERSION));

		// Check if MULTISAMPLE is avaiable
		int[] buf = new int[2];
		int[] samples = new int[2];
		gl.glGetIntegerv(GL2.GL_SAMPLE_BUFFERS, buf, 0);
		gl.glGetIntegerv(GL2.GL_SAMPLES, samples, 0);
		System.out.println("Number of sample buffers: " + buf[0]);
		System.out.println("Number of samples: " + samples[0]);

		// Defines frequency in which buffers (back and front) are changed
		gl.setSwapInterval(1);

		try {
			img_buff = ImageIO.read(new File("world.jpg"));
			// img_buff = ImageIO.read(new File("DukeWave.gif"));
		} catch (IOException e) {
		}
	}

	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
		if (root != null) {
			root.setSize(width, height);
		}
	}

	public void display(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();
		AgileState glState = AgileState.get(gl);

		// Call the glClear to clear the background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// Restore all the Java2D Graphics defaults
		jgraphics.resetAll(drawable);

		// Paint sample primitives
		jgraphics.setBackground(Color.WHITE);
		jgraphics.clearRect(0, 0, 1200, 800);

		this.step();
		if (interactive_antialias == true)
			jgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
		
		drawBigText(WIN_W, WIN_H, zFactor, jgraphics);
		/*
		switch (exampleNb) {
		case 1:
			drawBigText(WIN_W, WIN_H, zFactor, jgraphics);
			break;

		default:
			drawSimpleCurves(WIN_W, WIN_H, jgraphics);
			break;
		}
		*/
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void dispose(GLAutoDrawable drawable) {
	}

	public void setRoot(Component root) {
		this.root = root;
	}

	public Component getRoot() {
		return root;
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		keyPressed = e.getKeyCode();
		switch (keyPressed) {
		case KeyEvent.VK_F1:
			exampleNb = 1;
			break;
		case KeyEvent.VK_F2:
			exampleNb = 2;
			break;
		case KeyEvent.VK_F3:
			exampleNb = 3;
			break;
		case KeyEvent.VK_F4:
			exampleNb = 4;
			break;
		case KeyEvent.VK_F5:
			exampleNb = 5;
			break;
		case KeyEvent.VK_F6:
			exampleNb = 6;
			break;
		case KeyEvent.VK_F7:
			exampleNb = 7;
			break;
		case KeyEvent.VK_F8:
			exampleNb = 8;
			break;
		case KeyEvent.VK_F9:
			exampleNb = 9;
			break;

			// Other events
		case KeyEvent.VK_A:
			if (interactive_antialias == false) {
				interactive_antialias = true;
				System.out.println("Antialiasing is ON");
			} else {
				interactive_antialias = false;
				System.out.println("Antialiasing is OFF");
			}
			break;
		case KeyEvent.VK_R:
			rTheta += 0.15;
			break;
		case KeyEvent.VK_I:// zoom in
			zFactor += 0.1;
			break;
		case KeyEvent.VK_O:
			zFactor -= 0.1;
			break;
		case KeyEvent.VK_SPACE:
			System.out.println("Stop thread");
			this.stop();
			break;			
		}
		root.repaint();
	}

	public void keyReleased(KeyEvent e) {
	}


	public static void main(String[] args) {

		if (args.length == 0) {
			System.out
			.println("\nBad usage.\nYou must pass as an argument the type of component that you want to use: 'GLCanvas' (AWT component) or 'GLJPanel' (Swing component).");
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
			System.exit(0);
		}
		Frame frame = new Frame("Agile2D Demo");
		final AgileAnimeCanvas agile = new AgileAnimeCanvas(null);

		GLCapabilities glCaps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		glCaps.setDoubleBuffered(true);// request double buffer display mode
		glCaps.setSampleBuffers(true);
		glCaps.setNumSamples(NB_OF_SAMPLES_FOR_MULTISAMPLE);

		if (args[0].equals("GLCanvas")) {
			final GLCanvas canvas = new GLCanvas(glCaps);
			frame.add(canvas);
			canvas.addGLEventListener(agile);
			agile.setRoot(canvas);
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
		} else if (args[0].equals("GLJPanel")) {
			final GLJPanel panel = new GLJPanel(glCaps);
			frame.add(panel);
			panel.addGLEventListener(agile);
			agile.setRoot(panel);
		} else {
			System.out
			.println("\nBad usage.\nYou must specify the type of GL component that should be used: 'GLCanvas' (AWT component) or 'GLJPanel' (Swing component).\n");
			System.exit(0);
		}

		frame.setSize(WIN_W, WIN_H);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
		frame.addKeyListener(agile);
		agile.start();
	}

	// Sample display to test text rendering performance during zooming
	void drawBigText(int x, int y, double zoomFactor, AgileGraphics2D glGraphics) {
		glGraphics.scale(zoomFactor, zoomFactor);
		jgraphics.drawString("Test drawString", 0, 30);
		jgraphics.setColor(Color.GREEN);
		jgraphics.drawRect(4, 4, 120, 120);
		jgraphics.setColor(Color.RED);
		jgraphics.drawLine(3, 3, 130, 130);
	}

	// JAVA2D EXAMPLE CODE
	private static Color colors[] = { Color.blue, Color.green, Color.red };

	public void drawSimpleCurves(int w, int h, AgileGraphics2D glGraphics) {

		GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(w * .2f, h * .25f);

		// adds a cubic curve to the path
		p.curveTo(w * .4f, h * .5f, w * .6f, 0.0f, w * .8f, h * .25f);

		p.moveTo(w * .2f, h * .6f);

		// adds a quad curve to the path
		p.quadTo(w * .5f, h * 1.0f, w * .8f, h * .6f);

		glGraphics.setColor(Color.lightGray);
		glGraphics.fill(p);
		glGraphics.setColor(Color.black);
		glGraphics.draw(p);
		glGraphics.drawString("curveTo", (int) (w * .2), (int) (h * .25f) - 5);
		glGraphics.drawString("quadTo", (int) (w * .2), (int) (h * .6f) - 5);
	}
}
