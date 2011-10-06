/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;


import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import agile2d.geom.VertexArray;
import agile2d.geom.VertexArrayList;


/**
 * Render Fonts from their outlines
 *
 * @author
 * @version $Revision: 1.4 $
 */

class GlyphKey implements Comparable<GlyphKey>
{
	private String _key;
	private int _size;

	GlyphKey(char c_, Font f_)
	{
		_size = f_.getSize();
		_key = Character.toString(c_)+"_"+f_.getFontName()+"_"+f_.getStyle()+"_"+_size;

	}

	GlyphKey(GlyphVector gV_)
	{
		_key = Integer.toString(gV_.toString().hashCode());

	}

	//Create a key from an individual glyph (extracted from a glyphVector)
	GlyphKey(Font f_, int glyphCode_)
	{
		
		_size = f_.getSize();
		_key = glyphCode_+"_"+f_.getFontName()+"_"+f_.getStyle()+"_"+_size;
	}
	
	@Override
	public int compareTo(GlyphKey other)
	{
		return _key.compareTo(other._key);
	}

	@Override
	public boolean equals(Object other)
	{
		return (other != null) && (getClass() == other.getClass()) &&
		_key.equals(((GlyphKey)other)._key);
	}

	@Override
	public int hashCode()
	{
		return _key.hashCode();
	}

	@Override
	public String toString()
	{
		return _key;
	}

}


class OutlineRoughFontRenderer extends BasicOutlineFontRenderer {

	private VertexArrayList currentCharVAL;
	private SoftHashMap charSoftHashMap;

	private VertexArrayList currentGlyphVecVAL;
	private SoftHashMap glyphVecSoftHashMap;

	private VertexArrayList currentGlyphVAL;
	private SoftHashMap glyphSoftHashMap;	
	
	//This is the "by default" value of this parameter
	//the user may though control it by setting a render quality HINT
	//That's the role of method FontManager.setRoughOutlineQuality(int hint_)
	private static final float FONT_SIZE_INTERVAL_FACTOR = 1.2f;
	private static final int MIN_PRE_RENDER_FONT_SIZE = 12;
	private static final int MAX_PRE_RENDER_FONT_SIZE = 2048;
	private static final int INIT_FONT_SIZE_LENGTH = 64;

	private static final int CHARS_HASHMAP_SIZE = 32;
	private static final int GLYPHVECTORS_HASHMAP_SIZE = 32;
	private static final int GLYPHS_HASHMAP_SIZE = 512;

	private static int font_size_length;
	private static int listFontSizes[];

	//Static block to create list of font sizes that can be rendered in vertex arrays
	static{
		listFontSizes = new int[INIT_FONT_SIZE_LENGTH];
		float size_=(float)MIN_PRE_RENDER_FONT_SIZE;
		//The firt accepted size is the "starting size"
		listFontSizes[0] = (int)size_;
		int i=1;
		do{
			size_ *= FONT_SIZE_INTERVAL_FACTOR;
			listFontSizes[i]=(int)Math.round(size_);
			i++;
		}while( (i<INIT_FONT_SIZE_LENGTH) && (size_ < MAX_PRE_RENDER_FONT_SIZE) );
		font_size_length = i;

		System.out.print("Sizes: ");
		int j;
		for(j=0; j<font_size_length; j++)
			System.out.print(listFontSizes[j]+", ");

	}

	public int getNearestAboveFont(int reqSize_){
	//	System.out.println("Calling getNearestAboveSize");
		int length_ = listFontSizes.length;
		for(int i=0; i<length_; i++){
			if( listFontSizes[i] >= reqSize_ )
				return listFontSizes[i];
		}
		System.err.println("Warning: Can't find any size larger than the required size (which is "+reqSize_+").\n Using Max size, which is "+listFontSizes[length_-1]);
		return listFontSizes[length_-1];
	}


	public OutlineRoughFontRenderer(Tesselator tesselator) {
		this.tesselator = tesselator;
		charSoftHashMap = new SoftHashMap(CHARS_HASHMAP_SIZE);
		glyphVecSoftHashMap = new SoftHashMap(GLYPHVECTORS_HASHMAP_SIZE);
		glyphSoftHashMap = new SoftHashMap(GLYPHS_HASHMAP_SIZE);
	}

	public void render(GLAutoDrawable drawable, GlyphVector gV, double scale) {
		int i;
		GL2 gl = drawable.getGL().getGL2();
		currentGlyphVAL = null;
		for (i = 0; i < gV.getNumGlyphs(); i++) {
			if (installGlyph(drawable, gV, i) ) {
				//System.out.println("Number of glyphs in this vector :"+gV.getNumGlyphs());
				//DRAW A CHARACTER
				//i.e.: each VertexArrayList (many VertexArrays) corresponds to
				//a GlyphVector (many glyphs) and each vertexArray corresponds to
				//a polygon composing one glyph (a char may be composed of many glyphs)
				gl.glPushMatrix();
				gl.glScaled(scale, scale, 1.0);
				System.out.println("Vertices: "+currentGlyphVAL.size()+" and scale: "+scale);
				for (int j = 0; j < currentGlyphVAL.size(); j++){
					//System.out.println("Vertex: "+j+" and scale: "+scale);
					gl.glPushMatrix();
					gl.glTranslated(gV.getGlyphPosition(i).getX(), 0.0, 0.0);
					ShapeManager.render(gl, currentGlyphVAL.getVertexArrayAt(j), null);
					gl.glPopMatrix();
				}
				gl.glPopMatrix();
			}
		}
	}

	protected boolean installGlyph(GLAutoDrawable drawable, GlyphVector gV, int i_) {
		float temp_offset_x = 0.0f;
		GL2 gl = drawable.getGL().getGL2();

		//get VertexArray list of the glyph g
		GlyphKey tempKey_ = new GlyphKey( gV.getFont(), gV.getGlyphCode(i_) );
		//System.out.println("Temp key: "+tempKey_.toString());			
		currentGlyphVAL = (VertexArrayList)glyphSoftHashMap.get(tempKey_);
		//If data doesn't exist (still / anymore), make it
		if (currentGlyphVAL == null){
			temp_offset_x = (float)gV.getGlyphPosition(i_).getX();
			addTesselation(drawable, gV, i_, tempKey_, temp_offset_x);			
			System.out.println("No VertexArrayList for glyph: "+tempKey_.toString());			
		}
		else
			System.out.println("Found VertexArrayList for glyph: "+tempKey_.toString());
		return true;
	}
	
	//We will eat glyph by glyph and not bite a whole glyphVector
	public boolean addTesselation(GLAutoDrawable drawable, GlyphVector gV, int i_, GlyphKey key_, float offset_x) {
		//use the x offset so that the origin of all fonts geometry be "0"
		Shape s = gV.getGlyphOutline(i_, -offset_x, 0.0f);
		currentGlyphVAL = new VertexArrayList();
		VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(currentGlyphVAL);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
		glyphSoftHashMap.put((Object)key_, (Object)currentGlyphVAL);
		return true;
	}	

	//MUST implement these abstract methods since we inherit from BasicOutlineFontRender
	protected boolean addTesselation(GLAutoDrawable drawable, int c){return false;}
	public boolean installFont(GLAutoDrawable drawable, Font font_, double scale, boolean aa, boolean ufm){return false;}
	public void render(GLAutoDrawable drawable, String string, double scale, Font font_) {return;}
	
/*
	public boolean installFont(GLAutoDrawable drawable, Font font_, double scale, boolean aa, boolean ufm) {

		//Check if the requested font is the last one that has been used
		if (this.font != null && this.font.equals(font_)) {
			installed = true;
			return true;
		}
		CacheInfo info = findCached(font_);
		if (info == null) {
			installed = false;
			return false;
		}
		this.font = info.font;
		this.frc = new FontRenderContext(null, aa, ufm);
		this.metrics = info.metrics;

		//System.out.println("Scale :"+scale);

		//Create a new glyphvector from the current font
		setup();
		installed = true;
		return true;
	}	
	
	protected boolean addTesselation(GLAutoDrawable drawable, int c) {
		int charIndex = latin1Chars[c];
		Shape s = this.glyphs.getGlyphOutline(charIndex);
		if (s == null){
			System.out.println("Warning: There are no glyphs for this font");
			return false;
		}
		//get the metrics of this font that should be in the cache
		metrics[charIndex] = glyphs.getGlyphMetrics(charIndex);
		currentCharVAL = new VertexArrayList();
		VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(currentCharVAL);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
		GlyphKey tempGlyphKey_ = new GlyphKey((char)charIndex, this.font);
		charSoftHashMap.put((Object)tempGlyphKey_, (Object)currentCharVAL);
		return true;
	}

	//We try to store all the glyphVector in a VertexArrayList
	public boolean addTesselation(GLAutoDrawable drawable, GlyphVector gV) {
		Shape s = gV.getOutline();
		currentGlyphVecVAL = new VertexArrayList();
		VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(currentGlyphVecVAL);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);

		GlyphKey tempKey_ = new GlyphKey(gV);
		glyphVecSoftHashMap.put((Object)tempKey_, (Object)currentGlyphVecVAL);

		return true;
	}


	protected boolean installChar(GLAutoDrawable drawable, int c) {

		GL2 gl = drawable.getGL().getGL2();

		//get VertexArray list of the character c
		GlyphKey tempKey_ = new GlyphKey( (char) c, this.font);
		System.out.println("Temp key: "+tempKey_.toString());
		currentCharVAL = (VertexArrayList)charSoftHashMap.get(tempKey_);

		//If data doesn't exist (still / anymore), make it;
		if (currentCharVAL == null){
			addTesselation(drawable, c);
			System.out.println("No vertexArrayList for character: "+tempKey_.toString());
		}
		else
			System.out.println("Found VertexArrayList for character: "+tempKey_.toString());
		return true;
	}

	protected boolean installGlyphVector(GLAutoDrawable drawable, GlyphVector gV) {

		GL2 gl = drawable.getGL().getGL2();

		//get VertexArray list of the character c
		GlyphKey tempKey_ = new GlyphKey( gV );
		currentGlyphVecVAL = (VertexArrayList)glyphVecSoftHashMap.get(tempKey_);

		//If data doesn't exist (still / anymore), make it
		if (currentGlyphVecVAL == null){
			addTesselation(drawable, gV);
			//System.out.println("No VertexArrayList for glyphVector: "+gV.toString()+" with hashCode: "+tempKey_.toString());
			//for(int j=0; j<gV.getNumGlyphs(); j++)
			//		System.out.println("Character index of 1st glyph: "+gV.getGlyphCode(j));
		}
		//else
		//	System.out.println("Found VertexArrayList for glyphVector: "+gV.toString()+" with hashCode: "+tempKey_.toString());

		return true;
	}

	
	
	
	public void render(GLAutoDrawable drawable, String string, double scale, Font font_) {
		if (!installed)
			return;
		GL2 gl = drawable.getGL().getGL2();

		int i;
		for (i = 0; i < string.length(); i++) {
			int c = string.charAt(i);
			if (c > metrics.length)
				continue;
			currentCharVAL = null;
			if (installChar(drawable, c)) {
				//Get the metrics for each character
				GlyphMetrics m = metrics[c];
				//DRAW A CHARACTER
				//i.e.: each VertexArrayList (many VertexArrays) corresponds to
				//a character (many polygons) and each vertexArray corresponds to
				//a convex polygon composing the character
				gl.glPushMatrix();
				gl.glScaled(scale, scale, 1.0);
				for (int j = 0; j < currentCharVAL.size(); j++)
					ShapeManager.render(gl, currentCharVAL.getVertexArrayAt(j), null);
				gl.glPopMatrix();
				gl.glTranslated((m.getAdvanceX()*scale), (m.getAdvanceY()*scale), 0.0d);
			}
		}
		installed = false;
	}
*/
}
