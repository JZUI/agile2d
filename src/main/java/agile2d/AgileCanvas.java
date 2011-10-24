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
import java.awt.GraphicsEnvironment;

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
public class AgileCanvas implements GLEventListener, KeyListener {
	private AgileGraphics2D jgraphics;
	private Component       root;
	private BufferedImage img_buff = null;
	private Image img = null;
	private int keyPressed, exampleNb;
	private boolean interactive_antialias = false;
	private static int NB_OF_SAMPLES_FOR_MULTISAMPLE = 4;
	private double rTheta = 1.0;
	private double zFactor = 1.00;
	private int fontIndex;
	private Font[] allFonts;
	private int previousFontSize;

	/**
	 * Creates an Agile canvas from a Component.
	 * 
	 * @param root
	 *            the root component
	 */
	public AgileCanvas(Component root) {
		this.root = root;

	}

	/**
	 * {@inheritDoc}
	 */
	public void init(GLAutoDrawable drawable) {
		GLU glu = new GLU();
		jgraphics = AgileGraphics2D.getInstance(drawable);
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
		gl.setSwapInterval(1);


		try {
			img_buff = ImageIO.read(new File("world.jpg"));
		} catch (IOException e) {}

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		allFonts = ge.getAllFonts();
		fontIndex = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void reshape(GLAutoDrawable arg0, int x, int y, int width, int height) {
		if (root != null) {
			root.setSize(width, height);
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public void display(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();
		AgileState glState = AgileState.get(gl);

		// Call the glClear to clear the background
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// Restore all the Java2D Graphics defaults
		jgraphics.resetAll(drawable);

		//Paint sample primitives
		jgraphics.setBackground(Color.WHITE);
		jgraphics.clearRect(0, 0, 400, 300);

		if(interactive_antialias==true){
			jgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);			
		}

		switch(exampleNb){
		case 1:
			jgraphics.setColor(Color.GREEN);
			jgraphics.drawRect(90, 90, 120, 120);
			jgraphics.setColor(Color.YELLOW);
			jgraphics.fillRect(120, 120, 60, 60);
			jgraphics.setColor(Color.BLUE);
			jgraphics.drawString("Test drawString", 50, 50);		
			jgraphics.setColor(Color.RED);
			jgraphics.drawRoundRect(150, 150, 100, 100, 40, 40);
			jgraphics.setColor(Color.YELLOW);
			jgraphics.drawOval(100, 100, 120, 120);
			jgraphics.setColor(Color.BLUE);
			jgraphics.fillOval(150, 150, 60, 60);
			break;

		case 2:
			drawDemoFonts(400, 300, jgraphics);
			break;

		case 3:
			drawDemoAlpha(400, 300, jgraphics);
			break;

		case 4:
			drawDemoStrokes(400, 300, jgraphics);
			break;

		case 5:
			drawDemoCurves(400, 300, jgraphics);
			break;

		case 6:
			drawRotateArc(100, 100, rTheta, jgraphics);
			break;

			//switch font and drawBigText just after that
		case 10:
			switchFont(fontIndex, jgraphics);

		case 7:
			drawBigText(400, 300, zFactor, jgraphics);
			break;

		case 8:
			drawBigImage(5, 5, jgraphics);
			break;

		case 9:
			break;

		default:
			drawSimpleCurves(400, 300, jgraphics);
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void displayChanged(
			GLAutoDrawable drawable,
			boolean modeChanged,
			boolean deviceChanged) {
	}

	public void dispose(
			GLAutoDrawable drawable) {
	}

	/**
	 * Set the root
	 * 
	 * @param root
	 *            the new root
	 */
	public void setRoot(Component root) {
		this.root = root;
	}

	/**
	 * @return the root
	 */
	public Component getRoot() {
		return root;
	}

	public void keyTyped(KeyEvent e){}

	public void keyPressed(KeyEvent e){
		keyPressed = e.getKeyCode();
		switch(keyPressed){
		case KeyEvent.VK_F1:
			exampleNb=1;break;
		case KeyEvent.VK_F2:
			exampleNb=2;break;
		case KeyEvent.VK_F3:
			exampleNb=3;break;
		case KeyEvent.VK_F4:
			exampleNb=4;break;
		case KeyEvent.VK_F5:
			exampleNb=5;break;
		case KeyEvent.VK_F6:
			exampleNb=6;break;
		case KeyEvent.VK_F7:
			exampleNb=7;break;
		case KeyEvent.VK_F8:
			exampleNb=8;break;
		case KeyEvent.VK_F9:
			exampleNb=9;break;
			//Switch fonts
		case KeyEvent.VK_F:
			if(fontIndex < allFonts.length)
				fontIndex++;
			else
				fontIndex=0;
			exampleNb=10;
			break;

			//Other events
		case KeyEvent.VK_A:
			if(interactive_antialias==false){
				interactive_antialias=true;
				System.out.println("Antialiasing is ON");
			}
			else{
				interactive_antialias=false;
				System.out.println("Antialiasing is OFF");
			}
			break;
		case KeyEvent.VK_R:
			rTheta += 0.15;
			break;
		case KeyEvent.VK_I://zoom in
			zFactor += 0.1;
			break;
		case KeyEvent.VK_O:
			zFactor -= 0.1;
			break;

		}
		root.repaint();	
	}

	public void keyReleased(KeyEvent e){}

	/**
	 * Main program to test.

	 * 
	 * @param args
	 *            arg list
	 */
	public static void main(String[] args) {

		if(args.length == 0){
			System.out.println("\nBad usage.\nYou must pass as an argument the type of component that you want to use: 'GLCanvas' (AWT component) or 'GLJPanel' (Swing component).");
			System.out.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
			System.exit(0);
		}
		Frame frame = new Frame("Agile2D Demo");
		final AgileCanvas agile = new AgileCanvas(null);

		GLCapabilities glCaps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		glCaps.setDoubleBuffered(true);// request double buffer display mode
		glCaps.setSampleBuffers(true);
		glCaps.setNumSamples(NB_OF_SAMPLES_FOR_MULTISAMPLE);


		if(args[0].equals("GLCanvas")){
			final GLCanvas canvas = new GLCanvas(glCaps);
			frame.add(canvas);
			canvas.addGLEventListener(agile);
			agile.setRoot(canvas);
			System.out.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");System.out.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");System.out.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
		}
		else if(args[0].equals("GLJPanel")){
			final GLJPanel panel = new GLJPanel(glCaps);
			frame.add(panel);
			panel.addGLEventListener(agile);
			agile.setRoot(panel);
		}
		else{
			System.out.println("\nBad usage.\nYou must specify the type of GL component that should be used: 'GLCanvas' (AWT component) or 'GLJPanel' (Swing component).\n");
			System.exit(0);
		}

		frame.setSize(400, 300);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
		frame.addKeyListener(agile);
	}


	//Sample display to test tesselation while rotating an arc 
	void drawBigImage(int x, int y, AgileGraphics2D glGraphics){
		//		glGraphics.drawImage((Image)img_buff, 100, 100, 400, 300, 1000, 1000, 3000, 2000, null);
		glGraphics.drawImage((Image)img_buff, 0, 0, 1200, 800, null);
	}

	//Sample display to test tesselation while rotating an arc 
	void drawRotateArc(int x, int y, double rotation, AgileGraphics2D glGraphics){	
		glGraphics.setColor(Color.GREEN);
		glGraphics.translate(x, y);		
		glGraphics.rotate(rotation);		
		//		glGraphics.fillArc(0, 0, 200, 200, 0, 220);
		glGraphics.drawArc(0, 0, 200, 200, 0, 220);
	}



	//Sample display to test text rendering performance during zooming
	void drawBigText(int x, int y, double zoomFactor, AgileGraphics2D glGraphics){
		glGraphics.scale(zoomFactor, zoomFactor);
		glGraphics.drawString("Test drawString", 0, 30);
		glGraphics.setColor(Color.GREEN);
		//		glGraphics.drawRect(4, 4, 120, 120);
		glGraphics.setColor(Color.RED);
		glGraphics.drawLine(3, 3, 130, 130);
	}


	//
	//
	//
	//
	//JAVA2D EXAMPLE CODE
	//
	//
	//
	//





	private static Color colors[] = { Color.blue, Color.green, Color.red };


	public void switchFont(int fontIndex_, AgileGraphics2D glGraphics) {
		float previousFontSize_ = (float)(glGraphics.getFont()).getSize();
		System.out.println("Previous font size: "+(glGraphics.getFont()).getSize());
		Font nextFont_ = allFonts[fontIndex_].deriveFont(previousFontSize_);
		glGraphics.setFont(nextFont_);

		System.out.println("Present font name: "+(glGraphics.getFont()).getFontName());
	}


	public void drawDemoCurves(int w, int h, AgileGraphics2D glGraphics) {

		int y = 0;
		glGraphics.setColor(Color.black);

		// draws the word "QuadCurve2D"
		FontRenderContext frc = glGraphics.getFontRenderContext();
		TextLayout tl = new TextLayout("QuadCurve2D", glGraphics.getFont(), frc);
		float xx = (float) (w*.5-tl.getBounds().getWidth()/2);
		tl.draw(glGraphics, xx, tl.getAscent());

		// draws the word "CubicCurve2D"
		tl = new TextLayout("CubicCurve2D", glGraphics.getFont(), frc);
		xx = (float) (w*.5-tl.getBounds().getWidth()/2);
		tl.draw(glGraphics, xx, h*.5f);
		//        glGraphics.setStroke(new BasicStroke(5.0f));
		glGraphics.setStroke(new BasicStroke(5.0f));

		float yy = 20;

		// draws 3 quad curves and 3 cubic curves.
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				Shape shape = null;

				if (i == 0) {
					shape = new QuadCurve2D.Float(w*.1f,yy,w*.5f,50,w*.9f,yy);
				} else {
					shape = new CubicCurve2D.Float(w*.1f,yy,w*.4f,yy-15,
							w*.6f,yy+15,w*.9f,yy);
				}
				glGraphics.setColor(colors[j]);
				if (j != 2)
					glGraphics.draw(shape);

				if (j == 1 ) {
					glGraphics.setColor(Color.lightGray);

					/*
					 * creates an iterator object to iterate the boundary
					 * of the curve.
					 */
					 PathIterator f = shape.getPathIterator(null);

					/*
					 * while iteration of the curve is still in process
					 * fills rectangles at the endpoints and control
					 * points of the curve.
					 */
					 while ( !f.isDone() ) {
						 float[] pts = new float[6];
						 switch ( f.currentSegment(pts) ) {
						 case PathIterator.SEG_MOVETO:
						 case PathIterator.SEG_LINETO:
							 glGraphics.fill(new Rectangle2D.Float(pts[0], pts[1], 5, 5));
							 break;
						 case PathIterator.SEG_CUBICTO :
						 case PathIterator.SEG_QUADTO :
							 glGraphics.fill(new Rectangle2D.Float(pts[0], pts[1], 5, 5));
							 if (pts[2] != 0) {
								 glGraphics.fill(new Rectangle2D.Float(pts[2], pts[3], 5, 5));
							 }
							 if (pts[4] != 0) {
								 glGraphics.fill(new Rectangle2D.Float(pts[4], pts[5], 5, 5));
							 }
						 }
						 f.next();
					 }

				} else if (j == 2) {
					// draws red ellipses along the flattened curve.
					PathIterator p = shape.getPathIterator(null);
					FlatteningPathIterator f = new FlatteningPathIterator(p,0.1);
					while ( !f.isDone() ) {
						float[] pts = new float[6];
						switch ( f.currentSegment(pts) ) {
						case PathIterator.SEG_MOVETO:
						case PathIterator.SEG_LINETO:
							glGraphics.fill(new Ellipse2D.Float(pts[0], pts[1],3,3));
						}
						f.next();
					}
				}
				yy += h/6;
			}
			yy = h/2+15;
		}
	}



	public void drawDemoFonts(int w, int h, AgileGraphics2D glGraphics) {

		FontRenderContext frc = glGraphics.getFontRenderContext();
		Font f = new Font("sansserif",Font.PLAIN,w/8);
		Font f1 = new Font("sansserif",Font.ITALIC,w/8);
		String s = "AttributedString";
		AttributedString as = new AttributedString(s);

		/*
		 * applies the TextAttribute.Font attribute to the AttributedString 
		 * with the range 0 to 10, which encompasses the letters 'A' through
		 * 'd' of the String "AttributedString"
		 */ 
		as.addAttribute(TextAttribute.FONT, f, 0, 10 );

		/*
		 * applies the TextAttribute.Font attribute to the AttributedString 
		 * with the range 10 to the length of the String s, which encompasses
		 * the letters 'S' through 'g' of String "AttributedString"
		 */ 
		as.addAttribute(TextAttribute.FONT, f1, 10, s.length() );

		AttributedCharacterIterator aci = as.getIterator();

		// creates a TextLayout from the AttributedCharacterIterator
		TextLayout tl = new TextLayout (aci, frc);
		float sw = (float) tl.getBounds().getWidth();
		float sh = (float) tl.getBounds().getHeight();

		/*
		 * creates an outline shape from the TextLayout and centers it
		 * with respect to the width of the surface
		 */
		Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(w/2-sw/2, h*0.2+sh/2));
		glGraphics.setColor(Color.blue);
		glGraphics.setStroke(new BasicStroke(1.5f));
		glGraphics.draw(sha);
		glGraphics.setColor(Color.magenta);
		glGraphics.fill(sha);


		// creates a TextLayout from the String "Outline"
		f = new Font("serif", Font.BOLD,w/6);
		tl = new TextLayout("Outline", f, frc);
		sw = (float) tl.getBounds().getWidth();
		sh = (float) tl.getBounds().getHeight();
		sha = tl.getOutline(AffineTransform.getTranslateInstance(w/2-sw/2,h*0.5+sh/2));
		glGraphics.setColor(Color.black);
		glGraphics.draw(sha);
		glGraphics.setColor(Color.red);
		glGraphics.fill(sha);


		f = new Font("sansserif",Font.ITALIC,w/8);

		/*
		 * creates a new shearing AffineTransform 
		 */ 
		AffineTransform fontAT = new AffineTransform();

		fontAT.shear(-0.2, 0.0);

		// applies the fontAT transform to Font f
		Font derivedFont = f.deriveFont(fontAT);

		/*
		 * creates a TextLayout from the String "Italic-Shear" and with
		 * the transformed Font object
		 */
		tl = new TextLayout("Italic-Shear", derivedFont, frc);
		sw = (float) tl.getBounds().getWidth();
		sh = (float) tl.getBounds().getHeight();
		sha = tl.getOutline(AffineTransform.getTranslateInstance(w/2-sw/2,h*0.80f+sh/2));
		glGraphics.setColor(Color.green);
		glGraphics.draw(sha);
		glGraphics.setColor(Color.black);

		glGraphics.fill(sha);
	}


	public void drawSimpleCurves(int w, int h, AgileGraphics2D glGraphics) {

		GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		p.moveTo(w*.2f, h*.25f);

		// adds a cubic curve to the path
		p.curveTo(w*.4f, h*.5f, w*.6f, 0.0f, w*.8f, h*.25f);


		p.moveTo(w*.2f, h*.6f);

		// adds a quad curve to the path
		p.quadTo(w*.5f, h*1.0f, w*.8f, h*.6f);

		glGraphics.setColor(Color.lightGray);
		glGraphics.fill(p);
		glGraphics.setColor(Color.black);
		glGraphics.draw(p);
		glGraphics.drawString("curveTo", (int) (w*.2), (int) (h*.25f)-5);
		glGraphics.drawString("quadTo", (int) (w*.2), (int) (h*.6f)-5);
	}


	public void drawDemoAlpha(int w, int h, AgileGraphics2D glGraphics) {

		Color reds[] = { Color.red.darker(), Color.red };

		/*
		 * fills 18 Ellipse2D.Float objects, which get smaller as 
		 * N increases
		 */
		for (int N = 0; N < 18; N++) {

			float i = (N + 2) / 2.0f;
			float x = (float) (5+i*(w/2/10));
			float y = (float) (5+i*(h/2/10));
			float ew = (w-10)-(i*w/10);
			float eh = (h-10)-(i*h/10);

			/*
			 * assigns a higher value of alpha, corresponding to
			 * a higher value of N
			 */
			float alpha = (N == 0) ? 0.1f : 1.0f / (19.0f - N);

			// sets the ellipse to a darker version of red if N < 16
			if ( N >= 16 ) {
				glGraphics.setColor(reds[N-16]);
			} else {
				glGraphics.setColor(new Color(0f, 0f, 0f, alpha));
			}
			glGraphics.fill(new Ellipse2D.Float(x,y,ew,eh));
		}
	}


	public void drawDemoStrokes(int w, int h, AgileGraphics2D glGraphics) {

		FontRenderContext frc = glGraphics.getFontRenderContext();
		Font font = glGraphics.getFont();
		TextLayout tl = new TextLayout("Dashes", font, frc);
		float sw = (float) tl.getBounds().getWidth();
		float sh = (float) tl.getAscent() + tl.getDescent();
		glGraphics.setColor(Color.BLACK);
		tl.draw(glGraphics, (float) (w/2-sw/2), sh+5);

		BasicStroke dotted = new BasicStroke(3, BasicStroke.CAP_ROUND, 
				BasicStroke.JOIN_ROUND, 0, new float[]{6,6,6,6}, 0);

		glGraphics.setStroke(dotted);
		glGraphics.drawRect(3,3,w-6,h-6);

		int x = 0; int y = h-34;
		BasicStroke bs[] = new BasicStroke[6];

		float j = 1.1f;
		for (int i = 0; i < bs.length; i++, j += 1.0f) {
			float dash[] = { j };
			BasicStroke b = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
					BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
			glGraphics.setStroke(b);
			glGraphics.drawLine(20, y, w-20, y);
			bs[i] = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, 
					BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
			y += 5;
		}
		Shape shape = null;
		y = 0;

		for (int i = 0; i < 6; i++) {
			x = (i == 0 || i == 3) ? (w/3-w/5)/2 : x + w/3;
			y = (i <= 2) ? (int) sh+h/12 : h/2;

			glGraphics.setStroke(bs[i]);
			glGraphics.translate(x, y);  


			switch (i) {
			case 0 : shape = new Arc2D.Float(0.0f, 0.0f, w/5, h/4, 45, 270, Arc2D.PIE);
			break;
			case 1 : shape = new Ellipse2D.Float(0.0f, 0.0f, w/5, h/4);
			break;
			case 2 : shape = new RoundRectangle2D.Float(0.0f, 0.0f, w/5, h/4, 10.0f, 10.0f);
			break;
			case 3 : shape = new Rectangle2D.Float(0.0f, 0.0f, w/5, h/4);
			break;
			case 4 : shape = new QuadCurve2D.Float(0.0f,0.0f,w/10, h/2,w/5,0.0f);
			break;
			case 5 : shape = new CubicCurve2D.Float(0.0f,0.0f,w/15,h/2, w/10,h/4,w/5,0.0f);
			break;
			}

			glGraphics.draw(shape);
			glGraphics.translate(-x, -y);
		}
	}

}
