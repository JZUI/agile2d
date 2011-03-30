/*****************************************************************************
 * Copyright (C) 2011, Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/

package agile2d;

import agile2d.*;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.Component;
import java.awt.BasicStroke;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;

import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import java.nio.ByteBuffer;
//import com.sun.opengl.utils.BufferUtils;



/**
 * <b>AgileSample</b>
 * * @author Rodrigo de Almeida
 * @version $Revision$
 */
public class AgileSample implements GLEventListener, KeyListener {
    private AgileGraphics2D jgraphics;
    private Component root;
    private boolean interactive_antialias = false;
    private static int NB_OF_SAMPLES_FOR_MULTISAMPLE = 4;
    private boolean aglObjectCreated = false;
    private AglTestContext context;
    private int width, height;

    public void keyTyped(KeyEvent e){}

    public void keyPressed(KeyEvent e){}

    public void keyReleased(KeyEvent e){}

    public AgileSample(Component root) {this.root = root;}

    public AgileGraphics2D getAglInstance(){
	if (aglObjectCreated==true)
		return this.jgraphics;
	else{
		System.err.println("Error.\nAgileGraphics2D Object has not yet been created.");
		return null;
	}	
    }

    public void init(GLAutoDrawable drawable) {
	GLU glu = new GLU();
        jgraphics = new AgileGraphics2D(drawable);
	aglObjectCreated=true;
        GL2 gl = drawable.getGL().getGL2();
        System.out.println("INIT GL IS: " + gl.getClass().getName());
        System.out.println("GLU version is: " + glu.gluGetString(GLU.GLU_VERSION));

	//Check if MULTISAMPLE is avaiable	
	int[] buf = new int[2];
	int[] samples = new int[2];
	gl.glGetIntegerv(GL2.GL_SAMPLE_BUFFERS, buf, 0);
	gl.glGetIntegerv(GL2.GL_SAMPLES, samples, 0);
        System.out.println("Number of sample buffers: " + buf[0]);
        System.out.println("Number of samples: " + samples[0]);

	//Defines frequency in which buffers (back and front) are changed
	//gl.setSwapInterval(1);
  }

    public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
        if (root != null) {
            root.setSize(width, height);
        }
	this.width=width;
	this.height=height;
    }

    public void display(GLAutoDrawable drawable) {
	GL2 gl = drawable.getGL().getGL2();
        AgileState glState = AgileState.get(gl);
		
        // Call the glClear to clear the background
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // Restore all the Java2D Graphics defaults
        jgraphics.resetAll(drawable);
	jgraphics.setStroke(new BasicStroke(2));

	//Find out what is the standard Graphics2d Font setting
	Component c = (Component)drawable;
	Graphics2D g2d_sample = (Graphics2D)c.createImage(1, 1).getGraphics();
	jgraphics.setFont(g2d_sample.getFont());

	if(interactive_antialias==true)
		jgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

	//Paint sample primitives
	jgraphics.setBackground(Color.WHITE);
	jgraphics.clearRect(0, 0, width, height);
	
	//
	//call test methods if context has been created		
	if(context!=null)
	      	context.drawStrategy(jgraphics);
	else
		System.out.println("AglTestContext has NOT been created");

//	ByteBuffer buffer = ByteBuffer.allocateDirect(width*height*4);
//	gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
	
    }

    public void displayChanged(GLAutoDrawable drawable,boolean modeChanged,boolean deviceChanged) {}

    public void dispose(GLAutoDrawable drawable) {}

    public void setRoot(Component root) { this.root = root; }

    public Component getRoot() { return root; }

    public void setContext(AglTestContext context){ this.context = context; }

}
