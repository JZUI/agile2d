/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/

package agile2d;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import java.util.Hashtable;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

/**
 * Used to manage switching among possible drawString strategies.
 * 
 */
class FontManager {
	public static final int TEXTURE_STRATEGY = 0;
	public static final int OUTLINE_STRATEGY = 1;
	public static final int ROUGH_OUTLINE_STRATEGY = 2;

	public static final int MIN_QUALITY = 0;
	public static final int MEDIUM_QUALITY = 1;
	public static final int MAX_QUALITY = 2;
	
	private TextureFontRenderer textureFont;
	private OutlineFontRenderer outlineFont;
	private OutlineRoughFontRenderer roughOutlineFont;
	private int present_strategy;
	private int roughOutlineQuality;

	private static final boolean DEBUG_CHECK_GL = true;

	GL2  gl;
	AgileState glState;

	private AgileGraphics2D ag2d_active;
	private GLAutoDrawable drawable;
	private Font font;
	private double scale;
	private boolean frcAntialiasing;
	private boolean frcUsesFractionalMetrics;
	private boolean useFastShapes;
	private boolean incrementalFontHint;

	public void updateStates(AgileGraphics2D ag2d_active_, GLAutoDrawable drawable_, Font font_, double scale_, boolean frcAntialiasing_, boolean frcUsesFractionalMetrics_, boolean useFastShapes_){
		this.drawable = drawable_;
		this.font = font_;
		this.scale = scale_;
		this.frcAntialiasing = frcAntialiasing_;
		this.frcUsesFractionalMetrics = frcUsesFractionalMetrics_;
		this.useFastShapes = useFastShapes_;
		this.ag2d_active = ag2d_active_;
	}

	public FontManager(GL2 gl, TextureFontRenderer textureFont_, OutlineFontRenderer outlineFont_, OutlineRoughFontRenderer roughOutlineFont_) {
		this.gl = gl;
		this.glState = AgileState.get(gl);
		if(checkStrategy(TEXTURE_STRATEGY))
			setStrategy(TEXTURE_STRATEGY);
		//get pointers to different rendering strategies
		textureFont = textureFont_;
		outlineFont = outlineFont_;
		roughOutlineFont = roughOutlineFont_;
	}

	public void setStrategy(int strategyType){
			present_strategy = strategyType;
	}

	public int getStrategy(){
		return present_strategy;
	}

	public void setFont(Font _font){
		font = _font;	
	}

	public Font getFont(){
		return font;	
	}

	public void setRoughOutlineQuality(int qualityHint_){
		//System.out.println("Setting new hint to the quality of the roughOutlineRenderer");
		roughOutlineQuality = qualityHint_;	
	}

	public int getRoughOutlineuality(){
		return roughOutlineQuality;	
	}

	//check if a given strategy is working in a given state (in the context of a given set of state variables)
	//update state must be called juste before calling this function
	private boolean checkStrategy(int strategy_type_){
		switch(strategy_type_){
		case TEXTURE_STRATEGY:
			if (useFastShapes && textureFont.install(drawable, font, scale, frcAntialiasing, frcUsesFractionalMetrics))
				return true;
			else
				return false;
		case OUTLINE_STRATEGY:
			//Nothing to check
			return true;
		case ROUGH_OUTLINE_STRATEGY:
			//Nothing to check
			return true;
		default:
			System.err.println("Unknow strategy.\n Can't switch strategy.");
			return false;
		}
	}

	public void drawString(String string_){
		//By default, agile always try to use the texture strategy	
		if(checkStrategy(TEXTURE_STRATEGY))
			setStrategy(TEXTURE_STRATEGY);
		//if not, check if the current strategy(required) is valid
		else if(!checkStrategy(present_strategy)){
			System.err.println("Warning. Cannot call drawString since current drawString strategy cannot be supported.");
			return;
		}
		//then, check which strategy is on and call it
		switch(present_strategy){
			case TEXTURE_STRATEGY:
				//System.out.println("\nDrawing new string with texture strategy");
				_drawTextureString(string_);				
			break;
			case OUTLINE_STRATEGY:			
				//System.out.println("\nDrawing new string with outline strategy");
				//Too big to fit in a texture - draw from outlines instead				
				_drawOutlineString(string_);
			break;
			case ROUGH_OUTLINE_STRATEGY:			
				//System.out.println("\nDrawing new string with a rough outline strategy");
				//Too big to fit in a texture - draw from ROUGH outline of the shapes
				_drawRoughOutlineString(string_);
			break;
			default:
		}
	}

	private void checkForErrors() {
		// No error checking in JOGL - use a DebugGL instead
		// drawable.getGLContext().gljCheckGL();
	}


	private void _drawRoughOutlineString(String string) {
		double roughScale=1.0;
		//check if the fontSize required is different than that of the font object
		{
			int previousSize = font.getSize();
			int newRoughSize = roughOutlineFont.getNextUpperSize(previousSize);
			if(newRoughSize != previousSize){
				//System.out.println("Font size required: "+previousSize+". Size found and shrinked: "+newRoughSize);
				roughScale = (double)previousSize/newRoughSize;
				//if there's a size increase, insert this scale difference in the scale variable
				//scale *= roughScale;
				//get a new font instance with a size corresponding to the rough sizes
				Font previousFont_ = font;
				font = null;
				font = previousFont_.deriveFont((float)newRoughSize);
			}
		}
		if (roughOutlineFont.installFont(drawable, font, roughScale, frcAntialiasing, frcUsesFractionalMetrics)) {
			roughOutlineFont.render(drawable, string, roughScale, font);
		}
		if (DEBUG_CHECK_GL)
			checkForErrors();
	}

	private void _drawOutlineString(String string) {
		if (outlineFont.installFont(drawable, font, scale, frcAntialiasing, frcUsesFractionalMetrics)) {
			outlineFont.render(drawable, string, scale, font);
		}
		if (DEBUG_CHECK_GL)
			checkForErrors();
	}

	private void _drawTextureString(String string) {
		textureFont.setIncremental(incrementalFontHint);			
		//		doDisableAntialiasing();
		textureFont.render(drawable, string, scale, font);
		//		doEnableAntialiasing();

		ag2d_active.setPaint(ag2d_active.getPaint());
		if (DEBUG_CHECK_GL)
			checkForErrors();
	}

/*
	public void drawGlyphVector(GlyphVector g_){
		if( (present_strategy == TEXTURE_STRATEGY) && (checkStrategy(TEXTURE_STRATEGY)) ){
			// Fits in font cache - draw using texture memory
			_drawTextureGlyphVector(g_);
		}
		else {
			setStrategy(OUTLINE_STRATEGY);
			// Too big to fit in a texture - draw from outlines instead
			_drawOutlineGlyphVector(g_);
		}
	}
*/
		public void drawGlyphVector(GlyphVector gV){
		//By default, agile always try to use the texture strategy	
		if(checkStrategy(TEXTURE_STRATEGY))
			setStrategy(TEXTURE_STRATEGY);
		//if not, check if the current strategy(required) is valid
		else if(!checkStrategy(present_strategy)){
			System.err.println("Warning. Cannot call drawGlyphVector since current drawGlyphVector strategy cannot be supported.");
			return;
		}
		//then, check which strategy is on and call it
		switch(present_strategy){
			case TEXTURE_STRATEGY:
				//System.out.println("\nDrawing new glyphVector with texture strategy");
				_drawTextureGlyphVector(gV);				
			break;
			case OUTLINE_STRATEGY:			
				//System.out.println("\nDrawing new glyphVector with outline strategy");
				//Too big to fit in a texture - draw from outlines instead				
				_drawOutlineGlyphVector(gV);
			break;
			case ROUGH_OUTLINE_STRATEGY:			
				//System.out.println("\nDrawing new glyphVector with a rough outline strategy");
				//Too big to fit in a texture - draw from ROUGH outline of the shapes
				_drawRoughOutlineGlyphVector(gV);
			break;
			default:
		}
	}
	
	
	
	private void _drawTextureGlyphVector(GlyphVector g) {
		textureFont.setIncremental(incrementalFontHint);

		//			doDisableAntialiasing();
		textureFont.render(drawable, g, scale);
		//			doEnableAntialiasing();

		ag2d_active.setPaint(ag2d_active.getPaint());
		if (DEBUG_CHECK_GL)
			checkForErrors();
	}

	private void _drawOutlineGlyphVector(GlyphVector g) {
		if (outlineFont.prepareGlyphVertices(drawable)) {
			outlineFont.render(drawable, g);
		}
		if (DEBUG_CHECK_GL)
			checkForErrors();
	}

	private void _drawRoughOutlineGlyphVector(GlyphVector gV) {
		//check if the fontSize required is different than that of the font object
		{
			int previousSize = gV.getFont().getSize();
			int newRoughSize = roughOutlineFont.getNextUpperSize(previousSize);
			if(newRoughSize != previousSize){
				//System.out.println("Glyph size required: "+previousSize+". Size found and shrinked: "+newRoughSize);
				double roughScale = (double)previousSize/newRoughSize;
				//if there's a size increase, insert this scale difference in the scale variable
				scale *= roughScale;
				//get a new font instance with a size corresponding to the rough sizes
				Font previousFont_ = font;
				font = null;
				font = previousFont_.deriveFont((float)newRoughSize);
			}
		}
		roughOutlineFont.render(drawable, gV);
		if (DEBUG_CHECK_GL)
			checkForErrors();
	}
	
}

/*
//
//Original routines in AgileGraphics2D.java
//
		void doDrawString(String string, float x, float y) {
			if (font == null)
				return;
			gl.glPushMatrix();
			gl.glTranslated(x, y, 0);

			if (useFastShapes && textureFont.install(drawable, font, scale, frcAntialiasing, frcUsesFractionalMetrics)) {
				// Fits in font cache - draw using texture memory
					textureFont.setIncremental(incrementalFontHint);

				drawTextureString(string);
			//	System.out.println("Draw String on Texture");
			} else {
				// Too big to fit in a texture - draw from outlines instead
				drawOutlineString(string);
			//	System.out.println("Draw String as an outline... then tesselate");
			}


			gl.glPopMatrix();
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}


		void doDrawGlyphVector(GlyphVector g, float x, float y) {
			Font font = g.getFont();
			FontRenderContext frc = g.getFontRenderContext();

			gl.glPushMatrix();
			gl.glTranslatef(x, y, 0);

			if (useFastShapes &&
				textureFont.install(drawable, font, scale, frc.isAntiAliased(), frc.usesFractionalMetrics())) {
				// Fits in font cache - draw using texture memory
				textureFont.setIncremental(incrementalFontHint);
				_drawTextureGlyphVector(g);
			} else {
				// Too big to fit in a texture - draw from outlines instead
				_drawOutlineGlyphVector(g);
			}

			gl.glPopMatrix();
			if (DEBUG_CHECK_GL)
				checkForErrors();

		}
 */
