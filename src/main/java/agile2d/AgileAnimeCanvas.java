/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;

import agile2d.benchmark.*;


import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.Component;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import javax.media.opengl.GLEventListener;


/**
 * <b>AgileCanvas</b>
 *
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class AgileAnimeCanvas implements GLEventListener, KeyListener, Runnable {
	
	private final static int WIN_W = 1200;
	private final static int WIN_H = 800;
	private final static int NB_OF_SAMPLES_FOR_MULTISAMPLE = 4;

	private Chrono chrono;
	
	private AgileGraphics2D jgraphics;
	private Component root;
	private Thread thread;
	private AnimeBenchmark bench;

	private boolean interactive_antialias = false;
	private int keyPressed;
	
	public AgileAnimeCanvas(Component root) {
		this.root = root;
	}

	public void startAnim() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }


    public synchronized void stopAnim() {
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
    }

	public void init(GLAutoDrawable drawable) {
		GLU glu = new GLU();
		Component c = (Component)drawable;
		
		chrono = new Chrono();		
		bench = new AnimeBenchmark(chrono);
		
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
		bench.resetCounter();		
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


		if (interactive_antialias == true)
			jgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
		
		bench.drawBigText(WIN_W, WIN_H, jgraphics);
		bench.increment();
		bench.step();

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
		case KeyEvent.VK_SPACE:
			System.out.println("Stop thread");
			this.stopAnim();
			break;
		}
		root.repaint();
	}

	public void keyReleased(KeyEvent e) {
	}

/*
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

		agile.startAnim();
	}
	*/
}
