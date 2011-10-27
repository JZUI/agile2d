/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import agile2d.geom.VertexArray;
import agile2d.geom.VertexArraySupport;
import agile2d.geom.VertexAttributes;
import agile2d.ImageUtils;


/**
 * AgileGraphics2D implements a reasonably complete subset of the standard Java2D
 * Graphics2D API, using JOGL OpenGL as the rendering engine.
 *
 * <p>
 * Note that some API functions are marked "TBD" (to be done). Calling these
 * functions typically has no effect - most Java2D applications should  run
 * under OpenGL, although they may not appear correctly rendered.
 * </p>
 */
public final class AgileGraphics2D extends Graphics2D implements Cloneable, VertexArraySupport {
	private static volatile AgileGraphics2D instance = null;
	//
	// GRAPHICS STATE
	//
	private Paint           paint = Color.black;
	private Color           color = Color.black;
	private Color           background = Color.white;
	private Color           xorColor = null;
	private Composite       composite = AlphaComposite.SrcOver;
	private Stroke          stroke = DEFAULT_STROKE;
	private Font            font = DEFAULT_FONT;
	private RenderingHints  renderingHints = new RenderingHints(null);
	private AffineTransform transform = new AffineTransform();
	private Area            clipArea = null;
	private SavedClip       relClipArea = null;
	private Rectangle       tmpRect = new Rectangle();

	private class SavedClip extends Area {
		Area absClipArea;
		AffineTransform transform;

		SavedClip(Area absClipArea, AffineTransform transform) {
			super(absClipArea);
			this.absClipArea = absClipArea;
			this.transform = transform;
			try {
				transform(transform.createInverse());
			}
			catch (NoninvertibleTransformException e) {
				this.transform = null;
			}
		}
	}


	//
	// GRAPHICS ENGINE
	//
	// The engine is the object that does the drawing. Multiple Graphics2D
	// objects share one engine. Each engine is associated with a GLDrawable.
	//
	private GraphicsEngine engine = null;

	//
	// DEBUGGING CONSTANTS
	//
	// If this is true, all of the graphics drawn by AgileGraphics2D are rendered in
	// a rubycon red. Good for ensuring that you are really seing OpenGL rendering the
	// stuff, not Java2D.
	//
	private static final boolean DEBUG_SHOW_PAINT = false;

	// If this is true, after every GL Drawing request, a jglCheckGL is called to
	// generate any errors
	//
	private static final boolean DEBUG_CHECK_GL = true;

	// If this is true, clipping information is printed out to stdout
	//
	private static final boolean DEBUG_CLIP = false;

	// If this is true, messages are printed out when an unimplemented feature is called.
	//
	private static final boolean DEBUG_TBD = true;

	//
	// DEFAULTS
	//
	private static final AffineTransform IDENTITY = new AffineTransform();
	private static final BasicStroke     DEFAULT_STROKE = new BasicStroke();
	private static final Font            DEFAULT_FONT = new Font("Times", Font.BOLD, 12);
	static RenderingHints                HINTS = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	static {
		HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	static final int PAINT_SOLID = 0;
	static final int PAINT_GRADIENT = 1;
	static final int PAINT_TEXTURE = 2;

	public final static int ROUGH_SCALE_STRATEGY = 0;
	public final static int DEFAULT_STRATEGY = 1;

	// GraphicsEngine class
	//
	// In Java, many different Graphics objects can be created which refer
	// to the same underlying canvas - each with their own state.
	//
	// To support this, we define a GraphicsEngine class that contains all of the code
	// for talking to GL. The AgileGraphics2D class routes all requests to an engine object.
	//
	// The Engine owns all the GL specific code. The rest of the
	// AgileGraphics2D class contains only calls to the engine.
	//
	private static final class GraphicsEngine {
		AgileGraphics2D active; // Indicates which Graphics object is currently active

		// GL State
		GLAutoDrawable drawable;
		GL2 gl;
		GLU glu;
		private AgileState  glState;
		private double[] glMatrix = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

		private Tesselator        tesselator;
		private ShapeManager      shapeManager;
		private ImageManager      imageManager;
		private StencilManager    stencilManager;
		private GradientManager   gradientManager;

		private TextureFontRenderer textureFont;
		private OutlineFontRenderer outlineFont;
		private OutlineRoughFontRenderer outlineRoughFont;
		private FontManager fontManager;
		private Font              font;
		private FontRenderContext frc;
		private int preferedGlyphDrawStrategy;
		private int currentRenderingStrategy;

		//		private Shape             shapeClip;
		private TexturePaint      texturePaint;
		private double            scale;
		private double            lineWidth = 1.0;
		private double            absLineWidth = 1.0;
		private double            alpha = 1.0;
		private double[]          flatMatrix = new double[6];
		private Rectangle         windowBounds = null;
		private int               paintMode;

		private boolean           frcAntialiasing;
		private boolean           frcUsesFractionalMetrics;
		private boolean           inited;
		//		private boolean           usePixelAlignment;
		private boolean           useShapeClip;
		private boolean           isGLStencilAvailable;

		//
		// If useFastShapes is true, some shapes get drawn using more efficient GL routines.
		// If it is false, every shape (including text characters) goes through one of
		// three generic routines: doDrawShape, doFillShape or doFillImmutableShape. This
		// is needed by the GradientGraphicsEngine, which wants to draw all shapes differently
		//
		private boolean useFastShapes = true;

		// If isAffineMatrix is true, we can assume that OpenGL is using
		// the Java2D affine transform. If its false, then there may be
		// some custom non-affine matrix on the GL matrix stack (either a
		// projection or a modelview matrix).
		//
		private boolean isAffineMatrix = true;

		// Rendering hints
		private boolean  convexHint;
		private boolean  immutableShapeHint;
		private boolean  immutableImageHint;
		private boolean  incrementalFontHint;
		private boolean  antiAliasingHint;
		// will change the value when we can create a MULTISAMPLE_BUFFER with gl4java
		//		private boolean  aaEnabled = false;

		private double[] modelViewMatrix;
		private double[] projectionMatrix;

		// Prototype objects used for drawing round rects, ellipses and arcs
		RoundRectangle2D roundRectProto = new RoundRectangle2D.Double();
		Ellipse2D        ovalProto = new Ellipse2D.Double();
		Arc2D            arcProto = new Arc2D.Double();
		Line2D           lineProto = new Line2D.Double();
		Rectangle2D      rectProto = new Rectangle2D.Double();
		double[]         point = new double[2];
		Graphics2D       g2d;
		BufferedImage    buf;
		Graphics2D       bg;
		double		 maxLineWidth;
		double		 minLineWidth;
		int              maxTexSize;

		GraphicsEngine(GLAutoDrawable drawable) {
			this.drawable = drawable;
		}

		private void checkForErrors() {
			// No error checking in JOGL - use a DebugGL instead
			// drawable.getGLContext().gljCheckGL();
		}

		public void resetGL(GLAutoDrawable drawable) {
			this.drawable = drawable;
			this.gl = drawable.getGL().getGL2();
			this.glu = new GLU();
			//IndirectGL igl = (IndirectGL)gl;
			//igl.setGl(drawable.getGL());
		}

		void doInit() {
			if (inited)
				return;

			//this.gl = new IndirectGL(drawable.getGL());
			this.glu = new GLU();
			this.glState = AgileState.get(gl);
			//			glState.glSetShadeModel(GL2.GL_FLAT);
			//glState.glSetShadeModel(GL2.GL_SMOOTH);

			glState.glEnableClientState(GL2.GL_VERTEX_ARRAY);

			//Antialiasing of lines and points (are they necessary?)
			//glState.glEnable(GL2.GL_POINT_SMOOTH);
			//glState.glEnable(GL2.GL_LINE_SMOOTH);
			//glState.glEnable(GL2.GL_POLYGON_SMOOTH);

			//GL_BLEND should be enablend in order to apply antialiasing on lines and points
			//but, presently, it's by default disabled in doSetColor() routine
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

			//Check if the call below works (since it passes by a glState call and shall pass a pointer as argument)
			this.isGLStencilAvailable = (glState.getState(GL.GL_STENCIL_BITS) >= 1); // Need 1 bit for clip

			int nb_stencil_bits[] = new int[1];
			nb_stencil_bits[0] = 500;
			gl.glGetIntegerv(GL2.GL_STENCIL_BITS, nb_stencil_bits, 0);
			System.out.println("Gl STENCIL BITS:"+ nb_stencil_bits[0]);


			// For images
			maxTexSize = glState.getState(GL.GL_MAX_TEXTURE_SIZE);
			if (maxTexSize > ImageUtils.MAX_TEX_SIZE) { // limit texture size to MAX_TEX_SIZE
				maxTexSize = ImageUtils.MAX_TEX_SIZE;
			}
			buf = new BufferedImage(maxTexSize, maxTexSize, BufferedImage.TYPE_INT_ARGB);
			bg = (Graphics2D)buf.getGraphics();

			//The glState.getState(GL_LINE_WIDTH_RANGE) call would crash the
			//application since it doesn't return the state the value, instead we must
			//pass a pointer to a float[] so that the two state values be transmitted
			float maxLineRange[] = new float[2];
			float lineWidthGranularity[] = new float[1];
			gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, maxLineRange, 0);
			gl.glGetFloatv(GL2.GL_LINE_WIDTH_GRANULARITY, lineWidthGranularity, 0);
			//			System.out.println("Max Line min: "+maxLineRange[0]+" and max: "+maxLineRange[1]+" with a granularity of "+lineWidthGranularity[0]);
			minLineWidth = maxLineRange[0];
			maxLineWidth = maxLineRange[1];

			// We need a Java Graphics2D to fall back on in certain cases, so make one
			if (drawable instanceof Component) {
				Component c = (Component)drawable;

				g2d = (Graphics2D)c.createImage(1, 1).getGraphics();
				g2d.setRenderingHints(AgileGraphics2D.HINTS);
			} else {
				g2d = bg;
			}

			Dimension sz = new Dimension(drawable.getWidth(), drawable.getHeight());
			windowBounds = new Rectangle(0, 0, sz.width, sz.height);

			tesselator = new Tesselator(glu);
			shapeManager = new ShapeManager(tesselator, gl);
			imageManager = new ImageManager(gl, buf, bg);
			stencilManager = new StencilManager(gl);
			textureFont = new TextureFontRenderer();
			//			TextureFontRenderer.setMaxTextureSize(maxTexSize);
			outlineFont = new OutlineFontRenderer(tesselator);
			outlineRoughFont = new OutlineRoughFontRenderer(tesselator);
			fontManager = new FontManager(gl, textureFont, outlineFont, outlineRoughFont);
			//Strategy can be defined elsewhere as well
			//The important thin is to remember that it will impact both drawString and drawGlyphVector methods
			//That's why we call setStrategy() every time in the beginning of this methods
			//preferedGlyphDrawStrategy = FontManager.ROUGH_OUTLINE_STRATEGY;
			preferedGlyphDrawStrategy = FontManager.OUTLINE_STRATEGY;
			//The quality hint can be set only once
			fontManager.setRoughOutlineQuality(FontManager.MIN_QUALITY);
			currentRenderingStrategy = AgileGraphics2D.DEFAULT_STRATEGY;

			frcAntialiasing = false;
			frcUsesFractionalMetrics = false;
			//Get a fontRenderContext specific to the current FONT
			//Obs: maybe should be called only once when there's a setFont action
			g2d.setFont(this.font);
			frc = g2d.getFontRenderContext();

			gradientManager = new GradientManager(gl);

			inited = true;
			if (DEBUG_CHECK_GL)
				checkForErrors();
			
		}

		void doActivate(AgileGraphics2D g) {
			active = g;
		}

		//
		// Prepares the engine for rendering - resets the transform
		//
		void doReset() {
			Dimension sz = new Dimension(drawable.getWidth(), drawable.getHeight());
			windowBounds = new Rectangle(0, 0, sz.width, sz.height);

			isAffineMatrix = (modelViewMatrix == null && projectionMatrix == null);

			// Setup the Projection Matrix - note that we flip Y
			loadProjectionMatrix();

			shapeManager.flush(); // Deletes any unused call lists
			imageManager.flush(); // Deletes any unused call lists

			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doSetRenderingHints(RenderingHints hints) {
			convexHint = (hints.get(AgileRenderingHints.KEY_CONVEX_SHAPE_HINT) == Boolean.TRUE);
			//			convexHint = true;
			immutableShapeHint = (hints.get(AgileRenderingHints.KEY_IMMUTABLE_SHAPE_HINT) == Boolean.TRUE);
			immutableImageHint = (hints.get(AgileRenderingHints.KEY_IMMUTABLE_IMAGE_HINT) != Boolean.FALSE);
			incrementalFontHint = (hints.get(AgileRenderingHints.KEY_INCREMENTAL_FONT_RENDERER_HINT) != Boolean.FALSE);
			setProjectionMatrix((double[])hints.get(AgileRenderingHints.KEY_GL_PROJECTION_HINT));
			setModelViewMatrix((double[])hints.get(AgileRenderingHints.KEY_GL_MODELVIEW_HINT));
			frcAntialiasing =
				hints.get(RenderingHints.KEY_TEXT_ANTIALIASING) == RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
			frcUsesFractionalMetrics =
				hints.get(RenderingHints.KEY_FRACTIONALMETRICS) == RenderingHints.VALUE_FRACTIONALMETRICS_ON;
			boolean oldAA = antiAliasingHint;
			antiAliasingHint = (hints.get(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHints(hints);


			if (oldAA != antiAliasingHint) {
				if (antiAliasingHint) {
					doEnableAntialiasing();
				}
				else {
					doDisableAntialiasing();
				}
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doEnableAntialiasing() {
			if (! antiAliasingHint)
				return;
			glState.glEnable(GL.GL_MULTISAMPLE);
		}

		void doDisableAntialiasing() {
			glState.glDisable(GL.GL_MULTISAMPLE);
		}

		// PAINT AND COMPOSITE
		void doSetColor(Color color) {
			if (color == null)
				doSetColor(0);
			else {
				doSetColor(color.getRGB());
			}
		}

		void doSetColor(int argb) {
			useFastShapes = true;
			paintMode = PAINT_SOLID;

			byte a = (byte)((argb & 0xff000000) >> 24);
			byte r = (byte)((argb & 0xff0000) >> 16);
			byte g = (byte)((argb & 0x00ff00) >> 8);
			byte b = (byte)(argb & 0xff);

			if (DEBUG_SHOW_PAINT) {
				double lum = .30 * r + .59 * g + .11 * b;
				r = (byte)lum;
				g = 0;
				b = 0;
			}
			if (alpha != 1.0f) {
				if (a  < 0) {
					a = (byte)(alpha * (int)(256+a));
				}
				else {
					a = (byte)(alpha * (int)a);
				}

			}

			glState.glColor4ub(r, g, b, a);

			if (a != -1) {
				glState.glEnable(GL.GL_BLEND);
			}
			else {
				glState.glDisable(GL.GL_BLEND);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doSetGradient(GradientPaint gradient) {
			useFastShapes = false;
			paintMode = PAINT_GRADIENT;
			gradientManager.setGradient(gradient);
			gradientManager.setAlpha((float)alpha);
		}

		void doSetTexturePaint(TexturePaint tp) {
			useFastShapes = false;
			gl.glColor4f(1.0f, 1.0f, 1.0f, (float)alpha);
			paintMode = PAINT_TEXTURE;
			texturePaint = tp;
		}

		void doSetComposite(Composite composite) {
			if (composite instanceof AlphaComposite) {
				// (currently ignoring Blend Mode)
				AlphaComposite alphaComposite = (AlphaComposite)composite;
				alpha = alphaComposite.getAlpha();
				active.setPaint(active.paint);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		// CLIPPING
		// Sets the Clip rectangle in device coordinates. Disables
		// arbitrary shape clipping.
		//
		void doSetClip(Rectangle rect) {
			if (useShapeClip) {
				stencilManager.disableClipping(StencilManager.STENCIL_1);
				this.useShapeClip = false;
			}

			if (rect == null ||
					RectUtils.containsOrEquals(rect, windowBounds)) {
				// Disable clipping planes and scissor test
				glState.glDisable(GL.GL_SCISSOR_TEST);
				glState.glDisable(GL2.GL_CLIP_PLANE0);
				glState.glDisable(GL2.GL_CLIP_PLANE1);





				glState.glDisable(GL2.GL_CLIP_PLANE2);
				glState.glDisable(GL2.GL_CLIP_PLANE3);
			} else {
				if (DEBUG_CLIP) {
					System.out.println("CLIP: " + rect.x + " " +
							(windowBounds.height -
									(rect.y + rect.height)) + " " +
									rect.width + " " + rect.height);
				}

				if (isAffineMatrix) {
					// We know that the Java2D AffineTransform is the
					// only applicable transform.
					//
					// Use the Scissor test, which only works in 2D
					//
					gl.glScissor(rect.x - 1,
							windowBounds.height -
							(rect.y + rect.height) + 1, rect.width + 1,
							rect.height);
					glState.glEnable(GL.GL_SCISSOR_TEST);

					// Disable the clip planes just to be sure
					glState.glDisable(GL2.GL_CLIP_PLANE0);
					glState.glDisable(GL2.GL_CLIP_PLANE1);

					glState.glDisable(GL2.GL_CLIP_PLANE2);
					glState.glDisable(GL2.GL_CLIP_PLANE3);
				} else {
					//
					// May be a custom transform in use.
					// Use OpenGL Clip Planes, which work in 3D.
					//
					gl.glPushMatrix();
					loadIdentityMatrix();

					double[] plane = new double[4];

					plane[0] = 1;
					plane[1] = 0;
					plane[3] = -rect.x;
					gl.glClipPlane(GL2.GL_CLIP_PLANE0, plane, 0);

					plane[0] = -1;
					plane[1] = 0;
					plane[3] = rect.x + rect.width;
					gl.glClipPlane(GL2.GL_CLIP_PLANE1, plane, 0);

					plane[0] = 0;
					plane[1] = 1;
					plane[3] = -rect.y;
					gl.glClipPlane(GL2.GL_CLIP_PLANE2, plane, 0);

					plane[0] = 0;
					plane[1] = -1;
					plane[3] = rect.y + rect.height;
					gl.glClipPlane(GL2.GL_CLIP_PLANE3, plane, 0);

					glState.glEnable(GL2.GL_CLIP_PLANE0);
					glState.glEnable(GL2.GL_CLIP_PLANE1);
					glState.glEnable(GL2.GL_CLIP_PLANE2);
					glState.glEnable(GL2.GL_CLIP_PLANE3);

					glState.glDisable(GL.GL_SCISSOR_TEST);

					gl.glPopMatrix();
				}
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		// Sets a clip shape - this is specified in user coordinates
		//
		void doSetClipShape(Shape shape) {
			//
			// Need to render the shape into the Stencil buffer
			//
			stencilManager.begin(StencilManager.STENCIL_1, null);

			// Fill the shape - note that clips are specified
			// in world coordinates, so we need to not use an
			// identity transform...
			gl.glPushMatrix();
			gl.glLoadIdentity();
			shapeManager.fill(shape, null, (float)scale, false, false);
			gl.glPopMatrix();
			stencilManager.end();

			stencilManager.enableClipping(StencilManager.STENCIL_1);
			useShapeClip = true;
		}

		// FONT AND STROKE
		void doSetFont(Font font) {
			this.font = font;
			//Get a fontRenderContext specific to this new FONT
			g2d.setFont(font);
			frc = g2d.getFontRenderContext();
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doSetStroke(Stroke stroke) {
			if (stroke instanceof BasicStroke) {
				BasicStroke basic = (BasicStroke)stroke;
				lineWidth = basic.getLineWidth();
				absLineWidth = lineWidth * scale;

				//If this stroke uses a stroke pattern (e.g., dotted),
				//get the shape of the line and paint it as a shape
				if(basic.getDashArray() != null){
					useFastShapes=false;
					float[] dashArray = basic.getDashArray();
					//System.out.println("First entry of dash array (of length "+dashArray.length+"): "+dashArray[0]);
				}
				if (absLineWidth < maxLineWidth) {
					float t = (float)absLineWidth;
					if (t < 1)
						t = 1;
					gl.glLineWidth(t);
				}
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}


		// PAINT MODE
		void doSetPaintXOR(boolean xor) {
			if (xor) {
				glState.glLogicOp(GL.GL_XOR);
			} else {
				glState.glLogicOp(GL.GL_COPY);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		// TRANSFORMS
		private void loadProjectionMatrix() {
			gl.glMatrixMode(GL2.GL_PROJECTION);

			if (projectionMatrix == null) {
				gl.glLoadIdentity();
				glu.gluOrtho2D(0, windowBounds.width,
						windowBounds.height, 0);
			} else {
				gl.glLoadMatrixd(projectionMatrix, 0);
			}

			gl.glMatrixMode(GL2.GL_MODELVIEW);
		}


		private void loadIdentityMatrix() {
			if (modelViewMatrix == null) {
				gl.glLoadIdentity();
				// Red book, 3rd Edition, p. 677,
				// explains rasterization rules wrt
				// integer coordinates.
				// Filled shapes have integer pixels
				// coordinates whereas stroked
				// shapes coordinates are considered
				// from the center of the pixels.
				// Shifting the model coordinate system
				// fixes the problem, at a cost for
				// non accelerated graphics cards...
				gl.glTranslatef(0.375f, 0.375f, 0.0f);
			} else {
				gl.glLoadMatrixd(modelViewMatrix, 0);
			}
		}

		private void setProjectionMatrix(double[] val) {
			if (val == null && projectionMatrix == null) {
				// do nothing
			} else if (val == null && projectionMatrix != null) {
				projectionMatrix = null;
				loadProjectionMatrix();
				isAffineMatrix = (modelViewMatrix == null &&
						projectionMatrix == null);
			} else if (val != null && projectionMatrix == null) {
				projectionMatrix = new double[16];
				System.arraycopy(val, 0, projectionMatrix, 0, val.length);
				loadProjectionMatrix();
				isAffineMatrix = (modelViewMatrix == null &&
						projectionMatrix == null);
			} else if (val != null && projectionMatrix != null) {
				for (int i = 0; i < val.length; i++) {
					if (projectionMatrix[i] != val[i]) {
						System.arraycopy(val, 0, projectionMatrix, 0,
								val.length);
						loadProjectionMatrix();
						isAffineMatrix = (modelViewMatrix == null &&
								projectionMatrix == null);

						break;
					}
				}
			}
		}

		private void setModelViewMatrix(double[] val) {
			if (val == null && modelViewMatrix == null) {
				// do nothing
			} else if (val == null && modelViewMatrix != null) {
				modelViewMatrix = null;
				shapeManager.setModelViewMatrix(null);
				doSetTransform(active.transform);
				isAffineMatrix = (modelViewMatrix == null &&
						projectionMatrix == null);
			} else if (val != null && modelViewMatrix == null) {
				modelViewMatrix = new double[16];
				System.arraycopy(val, 0, modelViewMatrix, 0, val.length);
				shapeManager.setModelViewMatrix(modelViewMatrix);
				doSetTransform(active.transform);
				isAffineMatrix = (modelViewMatrix == null &&
						projectionMatrix == null);
			} else if (val != null && modelViewMatrix != null) {
				for (int i = 0; i < val.length; i++) {
					if (modelViewMatrix[i] != val[i]) {
						System.arraycopy(val, 0, modelViewMatrix, 0,
								val.length);
						shapeManager.setModelViewMatrix(modelViewMatrix);
						doSetTransform(active.transform);
						isAffineMatrix = (modelViewMatrix == null &&
								projectionMatrix == null);

						break;
					}
				}
			}
		}

		//
		// Called whenever the transform has changed - we need determine the
		// new line width in absolute coordinates. Hmm. expensive...
		//
		private void transformChanged() {
			AffineTransform transform = active.transform;

			//			usePixelAlignment = (isAffineMatrix ? true : false);

			if (transform.isIdentity()) {
				scale = 1;
			} else if (transform.getShearX() == 0 &&
					transform.getShearY() == 0) {
				scale = Math.abs(Math.max(transform.getScaleX(),
						transform.getScaleY()));
			} else if (transform.getScaleX() == 0 &&
					transform.getScaleY() == 0) {
				scale = Math.abs(Math.max(transform.getShearX(),
						transform.getShearY()));
			} else {
				// Turn off pixel alignment for text if there is a
				// general transform
				//				usePixelAlignment = false;

				// Transform a vector, then compute its length
				point[0] = 1;
				point[1] = 0;
				transform.deltaTransform(point, 0, point, 0, 1);
				scale = Math.sqrt((point[0] * point[0]) +
						(point[1] * point[1]));
			}

			absLineWidth = lineWidth * scale;
			if (absLineWidth <= 0)
				gl.glLineWidth(1);
			else if (absLineWidth < maxLineWidth) {
				gl.glLineWidth((float)absLineWidth);
			}
			shapeManager.setTolerance(1.0 / scale);
			active.relClipArea = null;
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		public void doScale(double sx, double sy) {
			gl.glScaled(sx, sy, 1);
			transformChanged();
		}

		public void doTranslate(double dx, double dy) {
			gl.glTranslated(dx, dy, 0);
			transformChanged();
		}

		public void doRotate(double theta) {
			gl.glRotated(Math.toDegrees(theta), 0, 0, 1);
			transformChanged();
		}

		void doSetTransform(AffineTransform transform) {
			loadIdentityMatrix();
			doTransform(transform);
		}

		void doTransform(AffineTransform aTransform) {
			switch (aTransform.getType()) {
			case AffineTransform.TYPE_TRANSLATION:
				gl.glTranslated(aTransform.getTranslateX(),
						aTransform.getTranslateY(), 0);
				break;
			case AffineTransform.TYPE_IDENTITY:
				break;
			case AffineTransform.TYPE_UNIFORM_SCALE:
				gl.glScaled(aTransform.getScaleX(), aTransform.getScaleX(), 1);
				break;
			case AffineTransform.TYPE_TRANSLATION | AffineTransform.TYPE_UNIFORM_SCALE:
				gl.glTranslated(aTransform.getTranslateX(),
						aTransform.getTranslateY(), 0);
				gl.glScaled(aTransform.getScaleX(), aTransform.getScaleX(), 1);
				break;
			default:
				aTransform.getMatrix(flatMatrix);

				// flatMatrix is m00 m10 m01 m11 m02 m12
				glMatrix[0] = flatMatrix[0];
				glMatrix[1] = flatMatrix[1];
				glMatrix[2] = 0;
				glMatrix[3] = 0;

				glMatrix[4] = flatMatrix[2];
				glMatrix[5] = flatMatrix[3];
				glMatrix[6] = 0;
				glMatrix[7] = 0;

				glMatrix[8] = 0;
				glMatrix[9] = 0;
				glMatrix[10] = 1;
				glMatrix[11] = 0;

				glMatrix[12] = flatMatrix[4];
				glMatrix[13] = flatMatrix[5];
				glMatrix[14] = 0;
				glMatrix[15] = 1;
				gl.glMultMatrixd(glMatrix, 0);
			}
			transformChanged();
		}

		//
		// DRAWING AND FILLING PRIMITIVES
		//
		// If useFastShapes is false, all of the drawing routines fallback
		// to one of two routines: drawShape or fillShape.
		//

		// STROKING
		void doDrawLine(float x1, float y1, float x2, float y2) {
			if (useFastShapes && absLineWidth < maxLineWidth) {
				gl.glBegin(GL.GL_LINES);
				gl.glVertex2f(x1, y1);
				gl.glVertex2f(x2, y2);
				gl.glEnd();
			} else {
				lineProto.setLine(x1, y1, x2, y2);
				drawShape(lineProto, null, false, true);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doDrawLines(int[] xPts, int[] yPts, int nPts, int mode) {
			if (useFastShapes && absLineWidth < maxLineWidth) {
				shapeManager.begin(mode);
				for (int i = 0; i < nPts; i++)
					shapeManager.addVertex(xPts[i], yPts[i]);
				shapeManager.end();
			} else if (nPts > 1) {
				// Convert to path
				GeneralPath path = new GeneralPath();
				path.moveTo(xPts[0], yPts[0]);

				for (int i = 1; i < nPts; i++) {
					path.lineTo(xPts[i], yPts[i]);
				}

				drawShape(path, null, false, false);
			}

			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doDrawRect(int x1, int y1, int width, int height) {
			int x2 = x1 + width;
			int y2 = y1;
			int x3 = x1 + width;
			int y3 = y1 + height;
			int x4 = x1;
			int y4 = y1 + height;

			if (useFastShapes) {
				if (absLineWidth < maxLineWidth) {
					// For 4 vertices, glBegin/glEnd is faster than vertex arrays
					gl.glBegin(GL.GL_LINE_LOOP);
					gl.glVertex2i(x1, y1);
					gl.glVertex2i(x2, y2);
					gl.glVertex2i(x3, y3);
					gl.glVertex2i(x4, y4);
					gl.glEnd();
				} else {
					float lw = (float)(lineWidth / 2.0);
					if (lw >= width || lw >= height) {
						gl.glRectf(x1-lw, y1-lw, x3+lw, y3+lw);
					}
					else {
						shapeManager.begin(GL2.GL_QUADS);
						shapeManager.addQuad(x1-lw, y1-lw, x2+lw, y2-lw, x2-lw, y2+lw, x1+lw, y1+lw);
						shapeManager.addQuad(x4-lw, y4+lw, x1-lw, y1-lw, x1+lw, y1+lw, x4+lw, y4-lw);
						shapeManager.addQuad(x4-lw, y4+lw, x3+lw, y3+lw, x3-lw, y3-lw, x4+lw, y4-lw);
						shapeManager.addQuad(x2+lw, y2-lw, x3+lw, y3+lw, x3-lw, y3-lw, x2-lw, y2+lw);
						shapeManager.end();
					}
				}
			} else {
				rectProto.setRect(x1, y1, width, height);
				drawShape(rectProto, null, false, true);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doDrawShape(Shape shape) {
			if (useFastShapes && absLineWidth < maxLineWidth) {
				// Fastest route - flatten the shape in object space and
				// draw the vertices using GL Lines.
				//
				PathIterator path = shape.getPathIterator(null, shapeManager.getTolerance()/scale);
				shapeManager.send(path, false);
			} else {
				drawShape(shape, null, immutableShapeHint, convexHint);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doDrawVertexArray(VertexArray array, VertexAttributes attributes) {
			drawShape(array, attributes, immutableShapeHint, false);

		}

		// All drawn shapes go through this primative method (unless faster methods for drawing exist)
		//
		private void drawShape(Shape shape, VertexAttributes attributes, boolean immutable, boolean convex) {
			if (paintMode != PAINT_SOLID) {
				// Push strokes drawn with a gradient through the fill routine...
				fillShape(active.stroke.createStrokedShape(shape), attributes, false, convex);
			} else {
				shapeManager.draw(shape, attributes, (float)scale, active.stroke, immutable, convex);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		// FILLING
		void doFillRect(double x1, double y1, double width, double height) {
			if (width <= 0 || height <= 0)
				return;
			if (useFastShapes) {
				gl.glRectd(x1, y1, x1 + width, y1 + height);
			} else {
				rectProto.setRect(x1, y1, width, height);
				fillShape(rectProto, null, false, true);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doFillPolygon(int[] xPts, int[] yPts, int nPts) {
			if (useFastShapes) {
				shapeManager.fill(xPts, yPts, nPts, convexHint);
			} else {
				Polygon p = new Polygon(xPts, yPts, nPts);
				fillShape(p, null, false, false);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		void doFillShape(Shape shape) {
			//			fillShape(shape, null, immutableShapeHint, convexHint);
			tesselator.fill(gl, shape,  null, 1);
		}

		void doFillShape(Shape shape, boolean convex) {
			fillShape(shape, null, immutableShapeHint, convex);
		}

		void doFillVertexArray(VertexArray array, VertexAttributes attributes) {
			fillShape(array, attributes, immutableShapeHint, false);
		}

		// All fill methods come here
		private void fillShape(Shape shape, VertexAttributes attributes, boolean immutable, boolean convex) {
			switch (paintMode) {
			case PAINT_SOLID:
				// Just fill the shape
				shapeManager.fill(shape, attributes, (float)scale, immutable, convex);
				break;
			case PAINT_GRADIENT:
				// Installs a 1D texture with a suitable texture generator
				// and fills the shape.
				gradientManager.begin((float)alpha);
				shapeManager.fill(shape, attributes, (float)scale, immutable, convex);
				gradientManager.end();
				break;
			case PAINT_TEXTURE:
				// Installs a 2D texture with S and T texture gen
				// and fills the shape.
				Texture texture = imageManager.findTexture(texturePaint.getImage(),
						null,
						immutableImageHint, true);

				if (texture != null) {
					texture.begin(texturePaint.getAnchorRect());
					shapeManager.fill(shape, attributes, (float)scale, immutable, convex);
					texture.end();
				}
				break;
			}

			if (DEBUG_CHECK_GL)
				checkForErrors();
		}


		// TEXT
		void doDrawString(String string, float x, float y) {
			if (font == null)
				return;
			gl.glPushMatrix();
			gl.glTranslated(x, y, 0);

			fontManager.setStrategy(this.preferedGlyphDrawStrategy);
			fontManager.updateStates(active, drawable, font, scale, frc, frcAntialiasing, frcUsesFractionalMetrics, useFastShapes);
			fontManager.drawString(string);

			gl.glPopMatrix();
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}



		void doDrawGlyphVector(GlyphVector g, float x, float y) {
			Font font = g.getFont();
			FontRenderContext frc_gv = g.getFontRenderContext();

			gl.glPushMatrix();
			gl.glTranslatef(x, y, 0);

			fontManager.setStrategy(this.preferedGlyphDrawStrategy);
			fontManager.updateStates(active, drawable, font, scale, frc_gv, frcAntialiasing, frcUsesFractionalMetrics, useFastShapes);
			fontManager.drawGlyphVector(g);

			gl.glPopMatrix();
			if (DEBUG_CHECK_GL)
				checkForErrors();

		}

		// IMAGES

		void doDrawTexture(Texture tex, double x1, double y1, double x2, double y2) {
			if (alpha == 0)
				return;
			glState.glColor4f(1, 1, 1, (float)alpha);
			tex.paint(x1, y1, x2, y2);
			active.setPaint(active.paint);
		}

		//		void doDrawImage(BufferedImage image, AffineTransform transform,
		//			int dx, int dy, int dwidth, int dheight) {
		//			int width = image.getWidth();
		//			point[0] = dx;
		//			point[1] = dy;
		//			transform.transform(point, 0, point, 0, 1);
		//			dx = (int)(point[0]);
		//			dy = (int)(point[1]);
		//
		//			// System.out.println("DRAW IMAGE " + dx + " " + dy + " " + dwidth + " " + dheight);
		//			// ONLY WORKS WITH GL.GL_RGB Images
		//			int[] data = ((DataBufferInt)(image.getWritableTile(0, 0)
		//				.getDataBuffer())).getData();
		//
		//			ImageUtils.convertAndFlipARGBtoRGBA(data, image.getWidth(),
		//				image.getHeight(), dwidth,
		//				dheight);
		//            IntBuffer buffer = IntBuffer.wrap(data);
		//
		//			gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 4);
		//			gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, width);
		//
		//			gl.glPushMatrix();
		//			gl.glLoadIdentity();
		//			gl.glRasterPos2i(dx, dy + dheight); // windowBounds.height - (dy+dheight));
		//			gl.glDrawPixels(dwidth, dheight, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
		//				buffer);
		//			gl.glPopMatrix();
		//
		//			gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, 0);
		//			if (DEBUG_CHECK_GL)
		//				checkForErrors();
		//		}

		void doCopyArea(Rectangle src, Rectangle dst) {
			int wasBlending = glState.getState(GL.GL_BLEND);
			gl.glReadBuffer(glState.getState(GL2.GL_DOUBLEBUFFER)!=0 ? GL.GL_BACK : GL.GL_FRONT);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			gl.glRasterPos2i(dst.x, dst.y + dst.height);
			gl.glCopyPixels(src.x,
					windowBounds.height - (src.y + src.height),
					src.width, src.height, GL2.GL_COLOR);
			gl.glPopMatrix();
			if (wasBlending != 0)
				glState.glEnable(GL.GL_BLEND);
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		// EXTENSION

		void doRunGL(GLEventListener ev) {
			try {
				glState.save();
				ev.init(drawable);
				ev.reshape(drawable, 0, 0, windowBounds.width, windowBounds.height);
				ev.display(drawable);
			}
			finally {
				glState.restore();
			}
		}
	}

	// SETUP
	//
	// Needed by the clone method
	//
	AgileGraphics2D() {
	}

	/**
	 * Constructs a AgileGraphics2D that renders to a particular GL Drawable.
	 * Users of AgileGraphics2D should call resetAll in their display methods
	 * to  make the OpenGL state reflect the default Java2D state.
	 * <pre>
	 *     aglGraphics.resetAll(); // Makes the OpenGL State reflect Java2D's defaults
	 *     aglGraphics...
	 * </pre>
	 *
	 * @param drawable the underlying GLDrawable
	 */

	//Old constructor (without singleton pattern)
	/*public AgileGraphics2D(GLAutoDrawable drawable) {
		engine = new GraphicsEngine(drawable);
	}*/

	//Disabled the public access to the constructor in order to adopt the Singleton pattern
	private AgileGraphics2D(GLAutoDrawable drawable) {
		super();
		engine = new GraphicsEngine(drawable);
	}

	public final static AgileGraphics2D getInstance(GLAutoDrawable drawable){
		if (AgileGraphics2D.instance == null) {
            // Syncronized keyword is essential in order to block multiple instantiation by different threads at work
            synchronized(AgileGraphics2D.class) {
              if (AgileGraphics2D.instance == null) {
            	  AgileGraphics2D.instance = new AgileGraphics2D(drawable);
              }
            }
         }
         return AgileGraphics2D.instance;
	}


	GLAutoDrawable getDrawable() {
		return engine.drawable;
	}

	/**
	 * Resets the Graphics to its default state.
	 */
	public void resetAll(GLAutoDrawable drawable) {
		engine.resetGL(drawable);
		engine.doInit();
		setBackground(Color.white);
		setColor(Color.black);
		setStroke(DEFAULT_STROKE);
		setTransform(IDENTITY);
		setFont(DEFAULT_FONT);
		setComposite(AlphaComposite.SrcOver);
		setPaintMode();
		setClip(null);
		clearRenderingHints();
		engine.doSetRenderingHints(renderingHints);
		engine.doReset();

		this.setRenderingStrategy(engine.currentRenderingStrategy);
	}

	/**
	 * Set glyph rendering strategy.
	 */
	public void setRenderingStrategy(int strat_) {
		if(engine.currentRenderingStrategy != strat_){
			engine.currentRenderingStrategy = strat_;
			switch(strat_){
			case DEFAULT_STRATEGY:
				engine.preferedGlyphDrawStrategy = FontManager.OUTLINE_STRATEGY;
				//enable high-precision scaling in texture font rendering
				engine.textureFont.setHighQuality(true);
				break;
			case ROUGH_SCALE_STRATEGY:
				engine.preferedGlyphDrawStrategy = FontManager.ROUGH_OUTLINE_STRATEGY;
				//disable high-precision scaling in texture font rendering
				engine.textureFont.setHighQuality(false);
				break;
			}
		}
	}

	public int getRenderingStrategy() {
		return engine.currentRenderingStrategy;
	}

	/**
	 * @see java.awt.Graphics#dispose()
	 */
	public void dispose() {
		// Not clear whether its safe to dispose of the drawable so we
		// just do nothing here.
	}


	//
	// Used by the create() method
	//
	/*
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() {
		AgileGraphics2D c = null;

		try {
			c = (AgileGraphics2D)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Object.clone failed.");
		}

		c.transform = new AffineTransform(transform);
		c.renderingHints = getRenderingHints();

		if (clipArea != null) {
			c.clipArea = (Area)(clipArea.clone());
		}
		c.relClipArea = null;

		return c;
	}

	private void makeCurrent() {
		if (engine.active != this) {
			engine.doActivate(this);

			setBackground(background);
			setPaint(paint);
			setStroke(stroke);
			setTransform(transform);
			setFont(font);
			setComposite(composite);
			updateClip();

			if (xorColor != null) {
				setXORMode(xorColor);
			} else {
				setPaintMode();
			}
		}
	}

	// STATE MANAGEMENT

	/**
	 * @see java.awt.Graphics2D#getBackground()
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * @see java.awt.Graphics2D#setBackground(java.awt.Color)
	 */
	public void setBackground(Color color) {
		makeCurrent();
		this.background = color;
	}

	/**
	 * @see       java.awt.Graphics#getColor()
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @see       java.awt.Graphics#setColor
	 */
	public void setColor(Color color) {
		makeCurrent();
		this.color = color;
		this.paint = color;
		engine.doSetColor(color);
	}

	/**
	 * AgileGraphics2D extension for specifing a color with an integer.
	 *
	 * @param argb color with 8 bits components for the alpha, red,
	 * green and blue channels in that order from high bits to low bits.

	 */
	public void setColor(int argb) {
		makeCurrent();
		this.color = null;
		this.paint = null;
		engine.doSetColor(argb);
	}

	/**
	 * @see java.awt.Graphics2D#getPaint()
	 */
	public Paint getPaint() {
		return paint;
	}

	/**
	 * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
	 */
	public void setPaint(Paint paint) {
		makeCurrent();
		if (paint instanceof Color) {
			// easy case
			setColor((Color)paint);
		} else if (paint instanceof GradientPaint) {

			GradientPaint gradient = (GradientPaint)paint;
			this.paint = paint;
			if (engine.isGLStencilAvailable) {
				// Only use gradients if there is a stencilManager buffer
				engine.doSetGradient(gradient);
			} else {
				// Just use a solid color which is half-way between the two endpoints
				Color c1 = gradient.getColor1();
				Color c2 = gradient.getColor2();
				int   red = (c1.getRed() + c2.getRed()) / 2;
				int   green = (c1.getGreen() + c2.getGreen()) / 2;
				int   blue = (c1.getBlue() + c2.getBlue()) / 2;
				setColor(new Color(red, green, blue));
			}
		} else if (paint instanceof TexturePaint) {
			engine.doSetTexturePaint((TexturePaint)paint);
		} else {
			setColor(Color.white); // throw new UnsupportedOperationException("Not Yet Implemented");
		}
	}

	/**
	 * @see java.awt.Graphics2D#getComposite()
	 */
	public Composite getComposite() {
		return composite;
	}

	/**
	 * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
	 */
	public void setComposite(Composite composite) {
		makeCurrent();
		this.composite = composite;
		engine.doSetComposite(composite);
	}

	/**
	 * @see java.awt.Graphics#setFont(java.awt.Font)
	 */
	public void setFont(Font font) {
		if (font == null)
			return;
		makeCurrent();
		this.font = font;
		engine.doSetFont(font);
	}

	/**
	 * @see java.awt.Graphics#getFont()
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
	 */
	@SuppressWarnings("deprecation")
	public FontMetrics getFontMetrics(Font f) {
		if (engine.g2d == null)
			return Toolkit.getDefaultToolkit().getFontMetrics(f);
		else
			return engine.g2d.getFontMetrics(f);
	}

	/**
	 * @see java.awt.Graphics2D#getStroke()
	 */
	public Stroke getStroke() {
		return stroke;
	}

	/**
	 * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
	 */
	public void setStroke(Stroke stroke) {
		makeCurrent();
		this.stroke = stroke;
		engine.doSetStroke(stroke);
	}

	/**
	 * @see java.awt.Graphics#setPaintMode()
	 */
	public void setPaintMode() {
		makeCurrent();
		engine.doSetPaintXOR(false);
		this.xorColor = null;
		setPaint(paint);
	}

	/**
	 * @see java.awt.Graphics#setXORMode(java.awt.Color)
	 */
	public void setXORMode(Color xorColor) {
		makeCurrent();
		engine.doSetPaintXOR(true);
		this.xorColor = xorColor;
		engine.doSetColor(xorColor);
	}

	// TRANSFORMS
	// Note that we need to maintain our own transform because there may be
	// multiple Graphics objects pointing to the same GL engine, each with their own
	// transform. For this reason, we perform all transform to our copy of the transform
	// and then load that into the GL engine.
	//
	/**
	 * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
	 */
	public void setTransform(AffineTransform tm) {
		makeCurrent();
		transform.setTransform(tm);
		engine.doSetTransform(transform);
	}

	/**
	 * @see java.awt.Graphics2D#getTransform()
	 */
	public AffineTransform getTransform() {
		return new AffineTransform(transform);
	}

	/**
	 * @see java.awt.Graphics2D#scale(double, double)
	 */
	public void scale(double sx, double sy) {
		makeCurrent();
		transform.scale(sx, sy);
		engine.doScale(sx, sy);
	}

	/**
	 * @see java.awt.Graphics2D#shear(double, double)
	 */
	public void shear(double sx, double sy) {
		makeCurrent();
		transform.shear(sx, sy);
		engine.doSetTransform(transform);
	}

	/**
	 * @see java.awt.Graphics2D#rotate(double, double, double)
	 */
	public void rotate(double theta, double x, double y) {
		makeCurrent();
		transform.rotate(theta, x, y);
		engine.doSetTransform(transform);
	}

	/**
	 * @see java.awt.Graphics2D#rotate(double)
	 */
	public void rotate(double theta) {
		makeCurrent();
		transform.rotate(theta);
		engine.doRotate(theta);
	}

	/**
	 * @see java.awt.Graphics2D#translate(double, double)
	 */
	public void translate(double x, double y) {
		makeCurrent();
		transform.translate(x, y);
		engine.doTranslate(x, y);
	}

	/**
	 * @see java.awt.Graphics#translate(int, int)
	 */
	public void translate(int x, int y) {
		if(x == 0 && y == 0)
			return;

		// Hmm - needs more thought?
		makeCurrent();
		transform.translate(x, y);
		engine.doTranslate(x, y);
	}

	/**
	 * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
	 */
	public void transform(AffineTransform transform) {
		makeCurrent();
		this.transform.concatenate(transform);
		engine.doTransform(transform);
	}

	// RENDERING HINTS
	private void clearRenderingHints() {
		renderingHints.clear();
		renderingHints.put(AgileRenderingHints.KEY_USING_GL_HINT, Boolean.TRUE);
		renderingHints.put(AgileRenderingHints.KEY_IMMUTABLE_IMAGE_HINT, Boolean.TRUE);
		renderingHints.put(AgileRenderingHints.KEY_INCREMENTAL_FONT_RENDERER_HINT, Boolean.FALSE);
		renderingHints.put(AgileRenderingHints.KEY_GL_DRAWABLE_HINT, engine.drawable);
	}

	/**
	 * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
	 */
	public void addRenderingHints(Map hints) {
		makeCurrent();
		renderingHints.putAll(hints);
		engine.doSetRenderingHints(renderingHints);
	}

	/**
	 * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
	 */
	public Object getRenderingHint(RenderingHints.Key key) {
		return renderingHints.get(key);
	}

	/**
	 * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key, java.lang.Object)
	 */
	public void setRenderingHint(RenderingHints.Key key, Object value) {
		makeCurrent();
		renderingHints.put(key, value);
		engine.doSetRenderingHints(renderingHints);
	}

	/**
	 * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
	 */
	public void setRenderingHints(Map hints) {
		makeCurrent();
		clearRenderingHints();
		renderingHints.putAll(hints);
		engine.doSetRenderingHints(renderingHints);
	}

	/**
	 * @see java.awt.Graphics2D#getRenderingHints()
	 */
	public RenderingHints getRenderingHints() {
		return (RenderingHints)renderingHints.clone();
	}

	// CLIPPING REGION
	private void updateClip() {
		makeCurrent();

		if (clipArea == null) {
			engine.doSetClip(null);
		} else if (clipArea.isRectangular()) {
			engine.doSetClip(clipArea.getBounds());
		} else {
			engine.doSetClipShape(clipArea);
		}
		relClipArea = null;
	}

	/**
	 * @see java.awt.Graphics#setClip(java.awt.Shape)
	 */
	public void setClip(Shape clip) {
		if (DEBUG_CLIP) {
			System.out.println("setClip(" + clip + ")");
		}

		if (clip == null) {
			clipArea = null;
			updateClip();
			return;
		} else if (clip instanceof SavedClip) {
			SavedClip saved = (SavedClip)clip;

			if (saved.transform.equals(transform)) {
				clipArea = saved.absClipArea;
				updateClip();
				relClipArea = saved;
				return;
			}
		}
		clipArea = new Area(clip);
		clipArea.transform(transform);
		updateClip();
	}

	/**
	 * @see         java.awt.Graphics#setClip(int,int,int,int)
	 */
	public void setClip(int x, int y, int width, int height) {
		tmpRect.setBounds(x, y, width, height);
		setClip(tmpRect);
	}

	/**
	 * @see java.awt.Graphics2D#clip(java.awt.Shape)
	 */
	public void clip(Shape clip) {
		if (DEBUG_CLIP) {
			System.out.println("clip(" + clip + ")");
		}

		Area a = new Area(clip);
		a.transform(transform);

		if (clipArea != null) {
			a.intersect(clipArea);
		}

		clipArea = a;

		updateClip();
	}

	/**
	 * @see java.awt.Graphics#clipRect(int, int, int, int)
	 */
	public void clipRect(int x, int y, int w, int h) {
		tmpRect.setBounds(x, y, w, h);
		clip(tmpRect);

		if (DEBUG_CLIP) {
			System.out.println("clipRect(" + tmpRect + ")");
		}
	}

	/**
	 * @see java.awt.Graphics#getClip()
	 */
	public Shape getClip() {
		if (clipArea == null)
			return null;
		if (relClipArea == null) {
			relClipArea = new SavedClip(clipArea, transform);
		}
		return relClipArea;
	}

	/**
	 * @see java.awt.Graphics#getClipBounds()
	 */
	public Rectangle getClipBounds() {
		return getClipBounds(new Rectangle());
	}

	/**
	 * @see java.awt.Graphics#getClipBounds(java.awt.Rectangle)
	 */
	public Rectangle getClipBounds(Rectangle r) {
		if (relClipArea != null) {
			r.setBounds(relClipArea.getBounds());
			return r;
		}
		Area a = clipArea;

		if (a == null) {
			a = new Area(engine.windowBounds);
		}

		try {
			AffineTransform tm = transform.createInverse();
			r.setBounds(a.createTransformedArea(tm).getBounds());
		} catch (NoninvertibleTransformException ex) {
			r.setBounds(engine.windowBounds);
		}

		return r;
	}

	/**

	 * @see java.awt.Graphics#hitClip(int, int, int, int)
	 */
	public boolean hitClip(int x, int y, int width, int height) {
		return new Rectangle(x, y, width, height).intersects(getClipBounds());
	}

	// STROKING PRIMITIVES

	/**
	 * @see java.awt.Graphics#drawLine(int, int, int, int)
	 */
	public void drawLine(int x1, int y1, int x2, int y2) {
		makeCurrent();
		engine.doDrawLine((float)x1, (float)y1, (float)x2, (float)y2);
	}


	/**
	 * @see         java.awt.Graphics#drawPolygon(int[], int[], int)
	 * @see java.awt.Graphics#drawPolyline(int[], int[], int)
	 */
	public void drawPolyline(int[] xPts, int[] yPts, int nPts) {
		makeCurrent();
		engine.doDrawLines(xPts, yPts, nPts, GL.GL_LINE_STRIP);
	}

	/**
	 * @see java.awt.Graphics#drawPolygon(int[], int[], int)
	 */
	public void drawPolygon(int[] xPts, int[] yPts, int nPts) {
		makeCurrent();
		engine.doDrawLines(xPts, yPts, nPts, GL.GL_LINE_LOOP);
	}

	/**
	 * @see java.awt.Graphics#drawRect(int, int, int, int)
	 */
	public void drawRect(int x1, int y1, int width, int height) {
		if ((width < 0) || (height < 0)) {
			return;
		}

		makeCurrent();
		engine.doDrawRect(x1, y1, width, height);
	}

	/**
	 * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
	 */
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		makeCurrent();
		engine.roundRectProto.setRoundRect(x, y, width, height, arcWidth,
				arcHeight);
		draw(engine.roundRectProto);
	}

	/**
	 * @see java.awt.Graphics#drawOval(int, int, int, int)
	 */
	public void drawOval(int x, int y, int width, int height) {
		makeCurrent();
		engine.ovalProto.setFrame(x, y, width, height);
		draw(engine.ovalProto);
	}


	/**
	 * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
	 */
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		makeCurrent();
		engine.arcProto.setArc(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
		draw(engine.arcProto);
	}

	/**
	 * @see java.awt.Graphics2D#draw(java.awt.Shape)
	 */
	public void draw(Shape shape) {
		makeCurrent();
		engine.doDrawShape(shape);
	}

	// FILLING PRIMITIVES

	/**
	 * @see java.awt.Graphics#fillRect(int, int, int, int)
	 */
	public void fillRect(int x1, int y1, int width, int height) {
		if ((width < 0) || (height < 0)) {
			return;
		}

		makeCurrent();
		engine.doFillRect(x1, y1, width, height);
	}

	/**
	 * @see java.awt.Graphics#clearRect(int, int, int, int)
	 */
	public void clearRect(int x, int y, int width, int height) {
		makeCurrent();

		Paint p = paint;
		setColor(background);
		fillRect(x, y, width, height);
		setPaint(p);
	}

	/**
	 * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
	 */
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		engine.roundRectProto.setRoundRect(x, y, width, height, arcWidth, arcHeight);
		makeCurrent();
		engine.doFillShape(engine.roundRectProto, true);
	}

	/**
	 * @see java.awt.Graphics#fillOval(int, int, int, int)
	 */
	public void fillOval(int x, int y, int width, int height) {
		engine.ovalProto.setFrame(x, y, width, height);
		makeCurrent();
		engine.doFillShape(engine.ovalProto, true);
	}

	/**
	 * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
	 */
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		engine.arcProto.setArc(x, y, width, height, startAngle, arcAngle, Arc2D.PIE);
		fill(engine.arcProto);
	}

	/**
	 * @see java.awt.Graphics#fillPolygon(int[], int[], int)
	 */
	public void fillPolygon(int[] xPts, int[] yPts, int nPts) {
		makeCurrent();
		engine.doFillPolygon(xPts, yPts, nPts);
	}

	/**
	 * @see java.awt.Graphics2D#fill(java.awt.Shape)
	 */
	public void fill(Shape shape) {
		makeCurrent();
		if (shape instanceof Rectangle2D) {
			Rectangle2D rect = (Rectangle2D) shape;
			engine.doFillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
		} else {
			engine.doFillShape(shape);

		}
	}


	// TEXT
	/**
	 * @see java.awt.Graphics#drawString(java.lang.String, int, int)
	 */
	public void drawString(String s, int x, int y) {
		drawString(s, (float)x, (float)y);
	}

	/**
	 * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
	 */
	public void drawString(String string, float x, float y) {
		makeCurrent();
		engine.doDrawString(string, x, y);
	}

	/**
	 * @see java.awt.Graphics#create()
	 */
	public Graphics create() {
		return (Graphics)clone();
	}

	/**
	 * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		Area a1 = new Area(new Rectangle(x, y, width, height));
		Area a2 = new Area(new Rectangle(dx, dy, width, height));
		a1.transform(transform);
		a2.transform(transform);

		Rectangle src = a1.getBounds();
		Rectangle dst = a2.getBounds();
		makeCurrent();
		engine.doCopyArea(src, dst);
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		int imgWidth = img.getWidth(observer);
		int imgHeight = img.getHeight(observer);

		return drawImage(img, x, y, x + imgWidth, y + imgHeight, 0, 0,
				imgWidth, imgHeight, null, observer);
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		int imgWidth = img.getWidth(observer);
		int imgHeight = img.getHeight(observer);

		return drawImage(img, x, y, x + width, y + height, 0, 0, imgWidth,
				imgHeight, null, observer);
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		int imgWidth = img.getWidth(observer);
		int imgHeight = img.getHeight(observer);

		return drawImage(img, x, y, x + imgWidth, y + imgHeight, 0, 0,
				imgWidth, imgHeight, bgcolor, observer);
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		int imgWidth = img.getWidth(observer);
		int imgHeight = img.getHeight(observer);

		return drawImage(img, x, y, x + width, y + height, 0, 0, imgWidth,
				imgHeight, bgcolor, observer);
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2,
			ImageObserver observer) {
		return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null,
				observer);
	}

	/**
	 * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, AffineTransform xform,
			ImageObserver obs) {
		AffineTransform tm = getTransform();
		transform(xform);

		boolean result = drawImage(img, 0, 0, obs);
		setTransform(tm);

		return result;
	}

	private boolean isTransformTranslation() {
		if (engine.scale != 1)
			return false;

		switch(transform.getType()) {
		case AffineTransform.TYPE_IDENTITY:
		case AffineTransform.TYPE_TRANSLATION:
			return true;
		default:
			return false;
		}
	}

	// ALL IMAGE METHODS GO VIA THIS ROUTINE
	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 */
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2,
			Color bgcolor, ImageObserver observer) {

		makeCurrent();

		// Ensure that sx/sy are positive (dx/dy can be negative)
		if (sx2 < sx1) {
			int t = sx2;


			sx2 = sx1;
			sx1 = t;
			t = dx2;
			dx2 = dx1;
			dx1 = t;
		}

		if (sy2 < sy1) {
			int t = sy2;
			sy2 = sy1;
			sy1 = t;
			t = dy2;
			dy2 = dy1;
			dy1 = t;
		}

		int imgWidth = img.getWidth(null);
		int imgHeight = img.getHeight(null);


		int src_w = sx2 - sx1;
		int src_h = sy2 - sy1;
		int dst_w = dx2 - dx1;
		int dst_h = dy2 - dy1;

		// Explore simple case we can handle right away
		if (! engine.immutableImageHint &&
				engine.alpha == 1 &&
				isTransformTranslation() &&
				(img instanceof BufferedImage) &&
				sx1 == 0 && sy1 == 0 &&
				src_w == dst_w && src_h == dst_h &&
				engine.imageManager.drawImage((BufferedImage)img, dx1, dy1, src_w, src_h)) {
			return true;
		}

		// SUPPORT IMAGES LARGER THAN MAX_TEX_SIZE x MAX_TEX_SIZE BY SHRINKING THEM TO FIT IN A TEXTURE
		//
		int sx1_, sy1_, dx1_, dy1_;
		sy1_ = sy1;
		dy1_ = dy1;
		if (src_w > engine.maxTexSize || src_h > engine.maxTexSize) {
			for (int delta_y = engine.maxTexSize; delta_y < (src_h+engine.maxTexSize); delta_y += engine.maxTexSize) {
				int sy2_, dy2_;
				if((delta_y > src_h) && engine.imageManager.texture_non_power_of_two==false){
					int resting_h = src_h-sy1_;
					delta_y = sy1_+ImageUtils.lowerPowerOf2(resting_h);
				}
				delta_y = Math.min(delta_y, src_h);
				sx1_ = sx1;
				dx1_ = dx1;
				sy2_ = sy1+delta_y;
				dy2_ = dy1 + ((delta_y * dst_h) / src_h);//delta_y must be deformed if src_w != dst_w
				for (int delta_x = engine.maxTexSize; delta_x < (src_w+engine.maxTexSize); delta_x += engine.maxTexSize) {
					int sx2_, dx2_;
					if((delta_x > src_w) && engine.imageManager.texture_non_power_of_two==false){
						int resting_w = src_w-sx1_;
						delta_x = sx1_+ImageUtils.lowerPowerOf2(resting_w);
					}
					delta_x = Math.min(delta_x, src_w);
					sx2_ = sx1+delta_x;
					dx2_ = dx1 + ((delta_x * dst_w) / src_w);//delta_x must be deformed if src_w != dst_w
					drawImage(img, dx1_, dy1_, dx2_, dy2_, sx1_, sy1_, sx2_, sy2_, bgcolor, observer);
					//					drawRect(dx1_, dy1_, dx2_-dx1_, dy2_-dy1_);//uncoment this line to see the layout of the tiles
					sx1_ = sx2_;
					dx1_ = dx2_;
				}
				sy1_ = sy2_;
				dy1_ = dy2_;
			}
			return true;
		}

		if (src_w <= 0 || src_h <= 0) {
			return true;
		}

		// Handle a background fill if present
		if (bgcolor != null) {
			Paint p = getPaint();
			setColor(bgcolor);
			engine.doFillRect(dx1, dy1, dst_w, dst_h);
			setPaint(p);
		}

		Rectangle rect;

		if (imgWidth == src_w && imgHeight == src_h) {
			rect = new Rectangle(0, 0, imgWidth, imgHeight);
		} else {
			rect = new Rectangle(sx1, sy1, sx2 - sx1, sy2 - sy1);
		}
		//		drawRect(rect.x, rect.y, rect.width, rect.height);

		Texture tex = engine.imageManager.findTexture(img, rect, engine.immutableImageHint, false);

		if (tex != null) {
			engine.doDrawTexture(tex, dx1, dy1, dx2, dy2);
		}

		return true;
	}

	/**
	 * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector, float, float)
	 */
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		makeCurrent();
		engine.doDrawGlyphVector(g, x, y);
	}

	/**
	 * Falls back on Java2D: Uses a Java2D graphics to do hit testing
	 *
	 * @see java.awt.Graphics2D#hit(Rectangle, Shape, boolean)
	 */
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		Graphics2D g2 = engine.g2d;
		g2.setTransform(transform);
		g2.setStroke(stroke);
		g2.setClip(clipArea);

		boolean result = g2.hit(rect, s, onStroke);
		g2.setTransform(IDENTITY);
		g2.setClip(null);

		return result;
	}

	/**
	 * Falls back on Java2D: Returns Java2D's current device configuration
	 *
	 * @see java.awt.Graphics2D#getDeviceConfiguration()
	 */
	public GraphicsConfiguration getDeviceConfiguration() {
		return engine.g2d.getDeviceConfiguration();
	}

	/**
	 * Returns new FontRenderContext(null, true, true).
	 *
	 * @return new FontRenderContext(null, true, true).
	 */
	public FontRenderContext getFontRenderContext() {
		return new FontRenderContext(null,
				engine.frcAntialiasing,
				engine.frcUsesFractionalMetrics);
	}

	// TBD: Graphics FUNCTIONS
	/**
	 * TBD
	 *
	 * @param iterator DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 */
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		if (DEBUG_TBD) {
			System.out.println("IGNORE: drawString(AttributedCharacterIterator,int,int)");
		}

		// TBD
	}

	// TBD: Graphics2D FUNCTIONS
	/**
	 * TBD
	 *
	 * @param img DOCUMENT ME!
	 * @param op DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 */
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		if (DEBUG_TBD) {
			System.out.println("IGNORE: drawImage(BufferedImage,BufferedImageOp,int,int)");
		}

		// TBD
	}

	/**
	 * TBD
	 *
	 * @param img DOCUMENT ME!
	 * @param xform DOCUMENT ME!
	 */
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		if (DEBUG_TBD) {
			System.out.println("IGNORE: drawRenderedImage(RenderedImage,AffineTransform)");
		}
	}

	/**
	 * TBD
	 *
	 * @param img DOCUMENT ME!
	 * @param xform DOCUMENT ME!
	 */
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		if (DEBUG_TBD) {
			System.out.println("IGNORE: drawRenderableImage(RenderableImage,AffineTransform)");
		}
	}

	/**
	 * TBD
	 *
	 * @param iterator DOCUMENT ME!
	 * @param x DOCUMENT ME!
	 * @param y DOCUMENT ME!
	 */
	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		if (DEBUG_TBD) {
			System.out.println("IGNORE: drawString(AttributedCharacterIterator,float,float)");
		}
	}


	/**
	 * Runs a GL program in the AgileGraphics2D context.
	 *
	 * The GL state is left as it was when the <code>GLEventListener</code>
	 * it called: transform, clip, states.
	 * All the graphic attributes are saved, but the GL state can
	 * still be broken if the stacks are popped for example.
	 *
	 * @param glevent the GLEventListener which will be called.
	 */
	public void runGL(GLEventListener glevent) {
		makeCurrent();
		engine.doRunGL(glevent);
	}

	/**
	 * Draws the stroke represented by a VertexArray, using the specified per-vertex colors specified in VertexAttributes.
	 */
	public void drawVertexArray(VertexArray array, VertexAttributes attributes) {
		makeCurrent();
		engine.doDrawVertexArray(array, attributes);
	}

	/**
	 * Fills the geometry specified in a VertexArray, using the specified per-vertex color attributes.
	 * If attributes is null, the geometry is filled with the current paint.
	 */
	public void fillVertexArray(VertexArray array, VertexAttributes attributes) {
		makeCurrent();
		engine.doFillVertexArray(array, attributes);
	}

}
