/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d.examples;

import java.awt.Color;
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
	private AgileGraphics2D jgraphics;
	private Component root;
	private int keyPressed;

	public AgileExample(Component root) {
		this.root = root;
	}

    public void reset(int w, int h) {
    }

	public void init(GLAutoDrawable drawable) {
		GLU glu = new GLU();
		jgraphics = AgileGraphics2D.getInstance(drawable);
		GL2 gl = drawable.getGL().getGL2();

		System.out.println("INIT GL IS: " + gl.getClass().getName());
		System.out.println("GLU version is: "
				+ glu.gluGetString(GLU.GLU_VERSION));
	}

	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
		if (root != null) {
			root.setSize(width, height);
			System.out.println("Resizing window to "+width+" x "+height);
		}
	}

	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// Call the glClear to clear the background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// Restore all the Java2D Graphics defaults
		jgraphics.resetAll(drawable);

		// Clear background
		jgraphics.setBackground(Color.WHITE);
		jgraphics.clearRect(0, 0, HelloWorld.WIN_W, HelloWorld.WIN_H);

		//Actually draw 'Hello World' string
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
			System.out.println("Do something");
			break;
		}
		root.repaint();
	}

	public void keyReleased(KeyEvent e) {
	}
}
