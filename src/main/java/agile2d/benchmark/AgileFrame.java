/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d.benchmark;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.*;

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
public class AgileFrame implements GLEventListener, KeyListener, Runnable {
	public final static int NB_OF_SAMPLES_FOR_MULTISAMPLE = 4;

	private BenchmarkGUI myParentRef;
	private Chrono chrono;	
	private AgileGraphics2D jgraphics;
	private Thread thread;
	private AnimeBenchmark bench;
	private int keyPressed;
	private boolean interactive_antialias = false;
	private int w, h;
	

	public void setRoot(BenchmarkGUI myParent_){
		myParentRef = myParent_;
	}
	
	public void setStrategy(int strat_){
		jgraphics.setRenderingStrategy(strat_);
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
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) { break; }
        }
        thread = null;
    }

    @Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
    	GLU glu = new GLU();
		chrono = new Chrono();		
		bench = new AnimeBenchmark(chrono);
		jgraphics = AgileGraphics2D.getInstance(drawable);
		
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
		gl.setSwapInterval(60);
		bench.resetCounter();
		//System.out.println("End of init");
		w = AnimeBenchmark.WIN_W;
		h = AnimeBenchmark.WIN_H;
	}
	
    @Override
	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
			w=width;
			h=height;
			System.out.println("Resizing window to "+w+" x "+h);
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		//System.out.println("Start of display");
		GL2 gl = drawable.getGL().getGL2();

		// Call the glClear to clear the background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// Restore all the Java2D Graphics defaults
		jgraphics.resetAll(drawable);

		// Paint sample primitives
		jgraphics.setBackground(Color.WHITE);
		jgraphics.clearRect(0, 0, w, h);

/*		if (interactive_antialias == true)
			jgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON);
*/		
		//bench.drawBigText(AnimeBenchmark.WIN_W, AnimeBenchmark.WIN_H, jgraphics);
		//System.out.println("Before scale");
		
		jgraphics.scale(bench.getZ(), bench.getZ());
		
		//System.out.println("AfterScale and before for loop");
		
		for(int i=0; i<(AnimeBenchmark.nb_repetitions*AnimeBenchmark.nb_fonts); i++){
			jgraphics.setFont(bench.getFont(i%AnimeBenchmark.nb_fonts));
			jgraphics.drawString("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 2, ((i+1)*AnimeBenchmark.INIT_FONT_SIZE));
		}
		
		//System.out.println("Before Increment");

		bench.increment();
		//System.out.println("Before Step");

		bench.step();
		//System.out.println("End of display");
		myParentRef.setFpsLabel(bench.getLastFPS());
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
		keyPressed = e.getKeyCode();
		switch (keyPressed) {
		case KeyEvent.VK_SPACE:
			System.out.println("Change strategy");
			if(jgraphics.getRenderingStrategy() == AgileGraphics2D.DEFAULT_STRATEGY)
				jgraphics.setRenderingStrategy(AgileGraphics2D.ROUGH_SCALE_STRATEGY);
			else if(jgraphics.getRenderingStrategy() == AgileGraphics2D.ROUGH_SCALE_STRATEGY)
				jgraphics.setRenderingStrategy(AgileGraphics2D.DEFAULT_STRATEGY);
			break;
		}
	}
	
	public long getLastFPS(){
		return bench.getLastFPS();
	}
	
	public void keyReleased(KeyEvent e) {
	}
}
