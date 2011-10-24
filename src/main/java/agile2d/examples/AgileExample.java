/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d.examples;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.Component;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLEventListener;

import agile2d.AgileGraphics2D;

/**
 * <b>AgileCanvas</b>
 * 
 */
public class AgileExample implements GLEventListener, KeyListener {
	public final static int NB_OF_SAMPLES_FOR_MULTISAMPLE = 2;

	private AgileGraphics2D jgraphics;
	private Component root;
	private int keyPressed;
	private boolean interactive_antialias = false;
	
	public AgileExample(Component root) {
		this.root = root;
	}

    public void reset(int w, int h) {
    }

	public void init(GLAutoDrawable drawable) {
		GLU glu = new GLU();		
		//jgraphics = new AgileGraphics2D(drawable);
		jgraphics = AgileGraphics2D.getInstance(drawable);
		GL2 gl = drawable.getGL().getGL2();
		
		System.out.println("INIT GL IS: " + gl.getClass().getName());
		System.out.println("GLU version is: "
				+ glu.gluGetString(GLU.GLU_VERSION));

		// Check if MULTISAMPLE is available
		int[] buf = new int[2];
		int[] samples = new int[2];
		gl.glGetIntegerv(GL2.GL_SAMPLE_BUFFERS, buf, 0);
		gl.glGetIntegerv(GL2.GL_SAMPLES, samples, 0);
		System.out.println("Number of sample buffers: " + buf[0]);
		System.out.println("Number of samples: " + samples[0]);
		// Defines frequency in which buffers (back and front) are changed
		gl.setSwapInterval(1);
	}

	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
		if (root != null) {
			root.setSize(width, height);
		}
	}

	public void display(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();

		// Call the glClear to clear the background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// Restore all the Java2D Graphics defaults
		jgraphics.resetAll(drawable);

		// Paint sample primitives
		jgraphics.setBackground(Color.WHITE);
		jgraphics.clearRect(0, 0, HelloWorld.WIN_W, HelloWorld.WIN_H);

		if (interactive_antialias == true)
			jgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
		
		HelloWorld.drawHelloWorld(jgraphics);
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
			break;
		}
		root.repaint();
	}

	public void keyReleased(KeyEvent e) {
	}
}
