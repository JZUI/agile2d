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

class CharKey implements Comparable<CharKey>
{
	private String _key;
	private int _size;

	CharKey(char c_, Font f_)
	{
		_size = f_.getSize();
		_key = Character.toString(c_)+"_"+f_.getFontName()+"_"+f_.getStyle()+"_"+_size;

	}

	CharKey(GlyphVector gV_)
	{
		_key = Integer.toString(gV_.toString().hashCode());

	}

	@Override
	public int compareTo(CharKey other)
	{
		return _key.compareTo(other._key);
	}

	@Override
	public boolean equals(Object other)
	{
		return (other != null) && (getClass() == other.getClass()) &&
		_key.equals(((CharKey)other)._key);
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

	//This is the "by default" value of this parameter
	//the user may though control it by setting a render quality HINT
	//That's the role of method FontManager.setRoughOutlineQuality(int hint_)
	private static final float FONT_SIZE_INTERVAL_FACTOR = 1.2f;
	private static final int MIN_PRE_RENDER_FONT_SIZE = 12;
	private static final int MAX_PRE_RENDER_FONT_SIZE = 2048;
	private static final int INIT_FONT_SIZE_LENGTH = 64;

	private static final int CHARS_HASHMAP_SIZE = 32;
	private static final int GLYPHVECTORS_HASHMAP_SIZE = 32;

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

	public int getNearestAboveSize(int reqSize_){
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
	}

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


	protected boolean installChar(GLAutoDrawable drawable, int c) {

		GL2 gl = drawable.getGL().getGL2();

		//get VertexArray list of the character c
		CharKey tempKey_ = new CharKey( (char) c, this.font);
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
		CharKey tempKey_ = new CharKey( gV );
		currentGlyphVecVAL = (VertexArrayList)glyphVecSoftHashMap.get(tempKey_);

		//If data doesn't exist (still / anymore), make it
		if (currentGlyphVecVAL == null){
			addTesselation(drawable, gV);
			/*
			System.out.println("No VertexArrayList for glyphVector: "+gV.toString()+" with hashCode: "+tempKey_.toString());
			for(int j=0; j<gV.getNumGlyphs(); j++)
					System.out.println("Character index of 1st glyph: "+gV.getGlyphCode(j));
			*/
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
				//gl.glTranslated(m.getAdvanceX(), m.getAdvanceY(), 0.0d);
			}
		}
		installed = false;
	}


	public void render(GLAutoDrawable drawable, GlyphVector gV) {
		int i;
		GL2 gl = drawable.getGL().getGL2();
		//for (i = 0; i < gV.getNumGlyphs(); i++) {
			if (installGlyphVector(drawable, gV) ) {
				//Get the metrics for each character
				//GlyphMetrics m = metrics[c];
				//DRAW A CHARACTER
				//i.e.: each VertexArrayList (many VertexArrays) corresponds to
				//a GlyphVector (many glyphs) and each vertexArray corresponds to
				//a polygon composing one glyph of part of one glyph
				double scale = 1.0d;
				gl.glPushMatrix();
				gl.glScaled(scale, scale, 1.0);
				for (int j = 0; j < currentGlyphVecVAL.size(); j++)
					ShapeManager.render(gl, currentGlyphVecVAL.getVertexArrayAt(j), null);
				gl.glPopMatrix();
				//gl.glTranslated((m.getAdvanceX())*scale, (m.getAdvanceY())*scale, 0.0d);
			}
		//}
	}

	//find what glyphs compose this char (with the current font)
	//and store the hascode of each glyph in an array hashmap
	protected void getGlyphsFromChar(char c_){
	}

	protected void drawChar(char c_){
		//Check if we already know what glyphs compose this character

		//if(charHashMap.get(charHashCode)==null)
			//getGlyphsFromChar(c_)

		//Fetch the array containing the hashcode of the characterGlyphs and print each of then

		//for(int i=0; i< charGlyphs.length; i++)
			//drawGlyph(charGlyphs(i));

	}

	protected void drawGlyph(int glyphHashCode){
		//call opengl drawArray operations for this glyph
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
		CharKey tempCharKey_ = new CharKey((char)charIndex, this.font);
		charSoftHashMap.put((Object)tempCharKey_, (Object)currentCharVAL);
		return true;
	}

	//We try to store all the glyphVector in a VertexArrayList
	public boolean addTesselation(GLAutoDrawable drawable, GlyphVector gV) {
		Shape s = gV.getOutline();
		currentGlyphVecVAL = new VertexArrayList();
		VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(currentGlyphVecVAL);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);

		CharKey tempKey_ = new CharKey(gV);
		glyphVecSoftHashMap.put((Object)tempKey_, (Object)currentGlyphVecVAL);

		return true;
	}

}
