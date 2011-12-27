/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d.benchmark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.*;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

import agile2d.AgileGraphics2D;

/**
 * <b>AgileCanvas</b>
 *
 */
public class AgileFrame implements GLEventListener, KeyListener{
	public final static int NB_OF_SAMPLES_FOR_MULTISAMPLE = 4;
	private Chrono chrono;
	private AgileGraphics2D jgraphics;
	private AnimeBenchmark bench;
	private int keyPressed;
	private int w, h;
	private static int current_strategy = AgileGraphics2D.BEST_TEXT_RENDERING_STRATEGY;

	public void setStrategy(int strat){
		current_strategy = strat;
		if(jgraphics!=null)
			jgraphics.setFontRenderingStrategy(current_strategy);
	}

	public int getStrategy(){
		if(jgraphics!=null)
			return jgraphics.getFontRenderingStrategy();
		else
			return current_strategy;
	}

	public AnimeBenchmark getRefToBench(){
		if(bench==null)
			System.out.println("GL Context not yet created.\nTrying agin.");
		return bench;
	}

    @Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
    	GLU glu = new GLU();
		chrono = new Chrono();
		bench = new AnimeBenchmark(chrono);
		AgileGraphics2D.destroyInstance();
		jgraphics = AgileGraphics2D.getInstance(drawable);
		jgraphics.setFontRenderingStrategy(current_strategy);

		System.out.println("INIT GL IS: " + gl.getClass().getName());
		System.out.println("GLU version is: "
				+ glu.gluGetString(GLU.GLU_VERSION));


		// Check if MULTISAMPLE is available
		int[] buf = new int[2];
		int[] samples = new int[2];
		gl.glGetIntegerv(GL2.GL_SAMPLE_BUFFERS, buf, 0);
		gl.glGetIntegerv(GL2.GL_SAMPLES, samples, 0);
		// Defines frequency in which buffers (back and front) are changed
		bench.resetCounter();
		w = AnimeBenchmark.WIN_W;
		h = AnimeBenchmark.WIN_H;

	}

    @Override
	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
			w=width;
			h=height;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// Call the glClear to clear the background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// Restore all the Java2D Graphics defaults
		jgraphics.resetAll(drawable);

		// Paint sample primitives
		jgraphics.setBackground(Color.WHITE);
		jgraphics.clearRect(0, 0, w, h);

		AnimeBenchmark.drawFullOvals(jgraphics);
		AnimeBenchmark.drawRects(jgraphics);
		AnimeBenchmark.drawImages(jgraphics);
		AnimeBenchmark.drawBigText(AnimeBenchmark.WIN_W, AnimeBenchmark.WIN_H, jgraphics);
		AnimeBenchmark.drawEmptyOvals(jgraphics);

		bench.increment();
		bench.step();

	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void keyTyped(KeyEvent e) {
	}


	public void keyPressed(KeyEvent e) {
	}

	public long getLastFPS(){
		if(bench!=null)
			return 1000;
		else
			return 0;
	}

	public void keyReleased(KeyEvent e) {
	}
}
