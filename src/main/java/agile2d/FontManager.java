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

/**
 * Used to manage switching among possible drawString strategies.
 * 
 */
class FontManager {
    public static final int STRING_AS_TEXTURE = 0;
    public static final int STRING_AS_OUTLINE = 1;

    private TextureFontRenderer textureFont;
    private OutlineFontRenderer outlineFont;
    private int present_strategy;

    private static final boolean DEBUG_CHECK_GL = true;

    GL2  gl;
    AgileState glState;

    private GLAutoDrawable drawable;
    private Font font;
    private double scale;
    private boolean frcAntialiasing;
    private boolean frcUsesFractionalMetrics;
    private boolean useFastShapes;

    public void updateStates(GLAutoDrawable drawable_, Font font_, double scale_, boolean frcAntialiasing_, boolean frcUsesFractionalMetrics_, boolean useFastShapes_){
    	this.drawable = drawable_;
	this.font = font_;
	this.scale = scale_;
	this.frcAntialiasing = frcAntialiasing_;
	this.frcUsesFractionalMetrics = frcUsesFractionalMetrics_;
	this.useFastShapes = useFastShapes_;
    }

    public FontManager(GL2 gl, TextureFontRenderer textureFont_, OutlineFontRenderer outlineFont_) {
        this.gl = gl;
        this.glState = AgileState.get(gl);
	if(!setStrategy(STRING_AS_TEXTURE))
		setStrategy(STRING_AS_OUTLINE);
	//get pointers to different rendering strategies
	textureFont = textureFont_;
	outlineFont = outlineFont_;
    }
    
    public boolean setStrategy(int strategyType){
	if(strategyType == present_strategy)
		return true;
	else{	
		present_strategy = strategyType;
		return true;
	}
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


   //check if a given strategy is working in a given state (in the context of a given set of state variables)
   //update state must be called juste before calling this function
   private boolean checkStrategy(int strategy_type_){
	switch(strategy_type_){
		case STRING_AS_TEXTURE:
			if (useFastShapes && textureFont.install(drawable, font, scale, frcAntialiasing, frcUsesFractionalMetrics))
				return true;
			else
				return false;
		case STRING_AS_OUTLINE:
			//Nothing to check
			return true;
		default:
			System.err.println("Unknow strategy.\n Can't switch strategy.");
			return false;
	}
   }

   public void drawString(String string_){
	if( (present_strategy == STRING_AS_TEXTURE) && (checkStrategy(STRING_AS_TEXTURE)) ){
		// Fits in font cache - draw using texture memory
		_drawTextureString(string_);
		System.out.println("Draw String on a texture");	
	}
	else {
		setStrategy(STRING_AS_OUTLINE);
		// Too big to fit in a texture - draw from outlines instead
		_drawOutlineString(string_);
		System.out.println("Draw String as an outline... then tesselate and fill it");
	}
   }
  
   private void checkForErrors() {
	// No error checking in JOGL - use a DebugGL instead
	// drawable.getGLContext().gljCheckGL();
   }
    
  /*
    public int getStrategyName(){
	//Return the name (i.e., a string) of the present strategy
	return string_strategies.get(present_strategy);
    }
*/

    private void _drawOutlineString(String string) {
	if (outlineFont.installFont(drawable, font, scale, frcAntialiasing, frcUsesFractionalMetrics)) {
		outlineFont.render(drawable, string, scale, font);
	}
	if (DEBUG_CHECK_GL)
		checkForErrors();
    }

    private void _drawTextureString(String string) {
		//textureFont.setIncremental(incrementalFontHint);			
//		doDisableAntialiasing();
		textureFont.render(drawable, string, scale, font);
//		doEnableAntialiasing();

//		active.setPaint(active.paint);
		if (DEBUG_CHECK_GL)
			checkForErrors();
	}

/*
		void drawGlyphVector(GlyphVector g, float x, float y) {
			Font font = g.getFont();
			FontRenderContext frc = g.getFontRenderContext();

			gl.glPushMatrix();
			gl.glTranslatef(x, y, 0);

			if (useFastShapes &&
				textureFont.install(drawable, font, scale, frc.isAntiAliased(), frc.usesFractionalMetrics())) {
				// Fits in font cache - draw using texture memory
				//textureFont.setIncremental(incrementalFontHint);
				drawTextureGlyphVector(g);
				System.out.println("Draw glyphVector as texture");	
			} else {
				// Too big to fit in a texture - draw from outlines instead
				drawOutlineGlyphVector(g);
				System.out.println("Draw glyphVector as outline");	
			}

			gl.glPopMatrix();
			if (DEBUG_CHECK_GL)
				checkForErrors();

		}

		private void _drawTextureGlyphVector(GlyphVector g) {
			doDisableAntialiasing();
			textureFont.render(drawable, g, scale);
			doEnableAntialiasing();

			active.setPaint(active.paint);
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}

		private void _drawOutlineGlyphVector(GlyphVector g) {
			Font font = g.getFont();

			FontRenderContext frc = g.getFontRenderContext();
			if (outlineFont.install(drawable, font, scale, frc.isAntiAliased(), frc.usesFractionalMetrics())) {
				outlineFont.render(drawable, g, scale);
			}
			if (DEBUG_CHECK_GL)
				checkForErrors();
		}
*/
}
