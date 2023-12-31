/*
 * Copyright (C) 2011 Jean-Daniel Fekete, Emmanuel Pietriga, Rodrigo Almeida*
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d.test;

import agile2d.*;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import javax.media.opengl.GLEventListener;
import java.nio.ByteBuffer;

/**
 * <b>AgileSample</b>
 * * @author Rodrigo de Almeida
 * @version $Revision$
 */
public class AgileSample implements GLEventListener, KeyListener {
    private AgileGraphics2D jgraphics;
    private Component root;
    private boolean aglObjectCreated = false;
    private AglTestContext context;
    private int width, height;
    private BufferedImage buf_img;

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
        jgraphics = AgileGraphics2D.getInstance(drawable);
        aglObjectCreated=true;
        GL2 gl = drawable.getGL().getGL2();
        System.out.println("INIT GL IS: " + gl.getClass().getName());
        System.out.println("GLU version is: " + glu.gluGetString(GLU.GLU_VERSION));

        //Set text rendering strategy to the best one since we are looking for render quality
        //ROUGH_TEXT_RENDERING_STRATEGY is the default one in order to obtain performance gains when zooming and animating text
        jgraphics.setFontRenderingStrategy(AgileGraphics2D.BEST_TEXT_RENDERING_STRATEGY);
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

        // Call the glClear to clear the background
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // Restore all the Java2D Graphics defaults
        jgraphics.resetAll(drawable);

        //Find out what is the standard Graphics2d Font setting
        Component c = (Component)drawable;
        Graphics2D g2d_sample = (Graphics2D)c.createImage(1, 1).getGraphics();

        //If gl_line_smooth is not enabled, stroke widths on java and opengl are identical
        //whereas if smooth is activated, opengl is equivalent to javaStroke+1
        //jgraphics.setStroke(new BasicStroke(1));

        //get the "default" font configuration (type and metrics)
        jgraphics.setFont(g2d_sample.getFont());

        //ANTIALIASING
        //By the time this test was written, agile2d antialiasing was difficult to handle in a generic way since:
        //1. to work on Linux, we must use GLJpanel instead of GLCanvas and we must set glCaps.setSampleBuffers(true)
        //2. to work on OSX, we must set glCaps.setSampleBuffers(false) but we can't desactivate it by the HINTS
        // jgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        jgraphics.setBackground(Color.WHITE);
        jgraphics.clearRect(0, 0, width, height);

        //
        //call test methods if context has been created
        if(context!=null){
            //Paint sample primitives
            context.drawStrategy(jgraphics);
        }
        else{
            System.out.println("AglTestContext has NOT been created");
        }
        //get a bufferedImage reading the window buffer via glReadPixels
        buf_img = createImageFromBuffer(gl);

    }

    public BufferedImage getBufferedImage(){
        return buf_img;
    }

    private BufferedImage createImageFromBuffer(GL2 gl){
        final int number_of_pixel_components = 4;
        ByteBuffer buffer = ByteBuffer.allocateDirect(width*height*number_of_pixel_components);
        gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
        BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixelInts = new int[ width*height ];

        int p = width * height * number_of_pixel_components;//Points to first byte (red) in each row.
        int q; // Index into ByteBuffer
        int i = 0; // Index into pixelInts
        int w4 = width * number_of_pixel_components; // Number of bytes in each row
        for(int row=0; row<height; row++){
            p -= w4;
            q = p;
            for(int col=0; col<width; col++){
                int iR = buffer.get(q++);
                int iG = buffer.get(q++);
                int iB = buffer.get(q++);
                q++;//ignores the last byte (alpha)
                pixelInts[i++] = 0xFF000000 | ((iR & 0x000000FF) << 16) | ((iG & 0x000000FF) << 8) | (iB & 0x000000FF);
            }
        }
        bImage.setRGB( 0, 0, width, height, pixelInts, 0, width);
        return bImage;
    }


    public void displayChanged(GLAutoDrawable drawable,boolean modeChanged,boolean deviceChanged) {}

    public void dispose(GLAutoDrawable drawable) {}

    public void setRoot(Component root) { this.root = root; }

    public Component getRoot() { return root; }

    public void setContext(AglTestContext context){ this.context = context; }

}
