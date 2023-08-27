/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

/**
 *  Manage switching among different font rendering strategies.
 *
 * @author Rodrigo de Almeida, Jean-Daniel Fekete
 * @version $Revision: 3.0 $
 */

class FontManager {
	public static final int TEXTURE_MODE = 0;
	public static final int OUTLINE_MODE = 1;

	public static final int PRECISE_OUTLINE_OPTION = 0;
	public static final int ROUGH_OUTLINE_OPTION = 1;

	public static final int MIN_QUALITY = 0;
	public static final int MEDIUM_QUALITY = 1;
	public static final int MAX_QUALITY = 2;

	//We set a "by default" strategy
	private static int current_strategy = AgileGraphics2D.ROUGH_TEXT_RENDERING_STRATEGY;

	private TextureFontRenderer textureFont;
	private OutlineFontRenderer outlineFont;
	private OutlineRoughFontRenderer roughOutlineFont;

	private int current_mode;
	private int current_outline_option;
	private int roughOutlineQuality;

	private static final boolean DEBUG_CHECK_GL = true;

	GL2  gl;
	AgileState glState;

	private AgileGraphics2D ag2d_active;
	private GLAutoDrawable drawable;
	private Font font;
	private FontRenderContext frc;
	private double scale;
	private boolean frcAntialiasing;
	private boolean frcUsesFractionalMetrics;
	private boolean useFastShapes;
	private boolean incrementalFontHint;

	public static void setInitRenderingStrategy(int init_strategy){
		current_strategy = init_strategy;
	}

	public FontManager(GL2 gl, TextureFontRenderer textureFont, OutlineFontRenderer outlineFont, OutlineRoughFontRenderer roughOutlineFont) {
		this.gl = gl;
		this.glState = AgileState.get(gl);
		if(isTextureModeSupported())
			current_mode = TEXTURE_MODE;
		//get pointers to different rendering strategies
		this.textureFont = textureFont;
		this.outlineFont = outlineFont;
		this.roughOutlineFont = roughOutlineFont;
		this.setRenderingStrategy(current_strategy);
	}

	public void updateStates(AgileGraphics2D ag2d_active, GLAutoDrawable drawable, Font font, double scale, FontRenderContext frc, boolean frcAntialiasing, boolean frcUsesFractionalMetrics, boolean useFastShapes){
		this.drawable = drawable;
		this.font = font;
		this.scale = scale;
		this.frc = frc;
		this.frcAntialiasing = frcAntialiasing;
		this.frcUsesFractionalMetrics = frcUsesFractionalMetrics;
		this.useFastShapes = useFastShapes;
		this.ag2d_active = ag2d_active;
		roughOutlineFont.updateActiveCopy(ag2d_active);
		outlineFont.updateActiveCopy(ag2d_active);
	}

	public void setRenderingStrategy(int strategy){
		current_strategy = strategy;
		if(current_strategy==AgileGraphics2D.BEST_TEXT_RENDERING_STRATEGY){
			textureFont.setHighQuality(true);
			current_outline_option = PRECISE_OUTLINE_OPTION;
		}
		else if(current_strategy==AgileGraphics2D.ROUGH_TEXT_RENDERING_STRATEGY){
			textureFont.setHighQuality(false);
			current_outline_option = ROUGH_OUTLINE_OPTION;
		}
	}

	public int getRenderingStrategy(){
		return current_strategy;
	}

	//Hints can be MIN, MEDIUM or MAX and indicates the "roughness" of the scale in which the glyphs of a given font are rendered
	public void setRoughOutlineQuality(int qualityHint){
		roughOutlineQuality = qualityHint;
	}

	public int getRoughOutlineQuality(){
		return roughOutlineQuality;
	}

	//check if a texture_strategy is working in a given state (in the context of a given set of state variables)
	//updateState must be called just before calling this
	private boolean isTextureModeSupported(){
		if (useFastShapes && textureFont.install(drawable, font, scale, frcAntialiasing, frcUsesFractionalMetrics))
			return true;
		else
			return false;
	}


	public void drawString(String string_){
		//By default, agile always try to use the texture strategy
		if(isTextureModeSupported())
			current_mode = TEXTURE_MODE;
		else
			current_mode = OUTLINE_MODE;
		//then, check which strategy is on and call it
		switch(current_mode){
			case TEXTURE_MODE:
				_drawTextureString(string_);
				break;
			//Too big to fit in a texture - draw from outlines instead
			case OUTLINE_MODE:
				if(current_outline_option==PRECISE_OUTLINE_OPTION)
					_drawOutlineString(string_);
				else if(current_outline_option==ROUGH_OUTLINE_OPTION)
					_drawRoughOutlineString(string_);
				break;
		}
	}

	private void checkForErrors() {
		// No error checking in JOGL - use a DebugGL instead
		// drawable.getGLContext().gljCheckGL();
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

	private void _drawOutlineString(String string) {
		//the block below temporarily cancel effect of the global scale transformation
		if(scale != 1.0){
			double scaledSize = font.getSize()*scale;
			Font previousFont_ = font;
			font = null;
			font = previousFont_.deriveFont((float)scaledSize);
		}
		gl.glPushMatrix();
		{
			gl.glScaled(1.0/scale, 1.0/scale, 1.0);
			if (outlineFont.installFont(drawable, font, scale, frcAntialiasing, frcUsesFractionalMetrics))
				outlineFont.render(drawable, string, scale, font);
		}
		gl.glPopMatrix();
		//End of temporary cancelling of global scale transformation

		if (DEBUG_CHECK_GL)
			checkForErrors();
	}

	private void _drawRoughOutlineString(String string) {
		double interScale, finalSize, aboveSize;
		//scale to shrink the outline (nearest upper available font size) to the demanded font size
		interScale=1.0;
		//check if the fontSize required is different than that of the font object
		{
			finalSize = font.getSize()*scale;
			aboveSize = roughOutlineFont.getNearestAboveFont((int)finalSize);
			if(aboveSize != finalSize){
				//System.out.println("Font size required: "+finalSize+". Size found and shrinked: "+aboveSize);
				interScale = (double)finalSize/aboveSize;
				//if there's a size increase, insert this scale difference in the scale variable
				//System.out.println("Scale: "+scale+" and interScale: "+interScale);
				//get a new font instance with a size corresponding to the rough sizes
				Font previousFont_ = font;
				font = null;
				font = previousFont_.deriveFont((float)aboveSize);
			}
		}

		GlyphVector tempGV = font.createGlyphVector(frc, string);
		//the block below temporarily cancel effect of the global scale transformation
		gl.glPushMatrix();
		{
			gl.glScaled(1.0/scale, 1.0/scale, 1.0);
			_drawRoughOutlineGlyphVector(tempGV, interScale);
			//if (roughOutlineFont.installFont(drawable, font, interScale, frcAntialiasing, frcUsesFractionalMetrics))
			//	roughOutlineFont.render(drawable, string, interScale, font);
		}
		gl.glPopMatrix();
		//End of temporary canceling of global scale transformation

		if (DEBUG_CHECK_GL)
			checkForErrors();
	}



	public void drawGlyphVector(GlyphVector gV){
		//By default, agile always try to use the texture mode
		if(isTextureModeSupported())
			current_mode = TEXTURE_MODE;
		else
			current_mode = OUTLINE_MODE;
		//then, check which mode is on and call it
		switch(current_mode){
		case TEXTURE_MODE:
			_drawTextureGlyphVector(gV);
			break;
		case OUTLINE_MODE:
			//Too big to fit in a texture - draw from outlines instead
			if(current_outline_option==PRECISE_OUTLINE_OPTION)
				_drawOutlineGlyphVector(gV);
			else if(current_outline_option==ROUGH_OUTLINE_OPTION)
				_drawRoughOutlineGlyphVector(gV, 1.0);
			break;
		default:
			System.out.println("Error. Unknow MODE for rendering glyphs.");
		break;
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

	private void _drawRoughOutlineGlyphVector(GlyphVector gV, double interScale_) {
		//the block below temporarily cancel effect of the global scale transformation
		gl.glPushMatrix();
		{
			gl.glScaled(1.0/scale, 1.0/scale, 1.0);
			roughOutlineFont.render(drawable, gV, (scale*interScale_));
		}
		gl.glPopMatrix();
		//End of temporary cancelling of global scale transformation
		if (DEBUG_CHECK_GL)
			checkForErrors();
	}
}
