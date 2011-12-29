/*****************************************************************************
 * Copyright (C) 2011, Rodrigo ALmeida
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;

import agile2d.geom.VertexArrayList;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Render Fonts from their outlines
 *
 * @author
 * @version $Revision: 1.4 $
 */

class GlyphKey implements Comparable<GlyphKey>
{
	private String _key;

	//Create a key from an individual glyph (extracted from a glyphVector)
	GlyphKey(Font f_, int glyphCode_)
	{
		_key = glyphCode_+"_"+f_.getFontName()+"_"+f_.getStyle()+"_"+f_.getSize();
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
	//This is the "by default" value of this parameter
	//the user may though control it by setting a render quality HINT
	//That's the role of method FontManager.setRoughOutlineQuality(int hint_)
	private static final float FONT_SIZE_INTERVAL_FACTOR = 1.2f;
	private static final int MIN_PRE_RENDER_FONT_SIZE = 12;
	private static final int MAX_PRE_RENDER_FONT_SIZE = 2048;
	private static final int INIT_FONT_SIZE_LENGTH = 64;
	private static final int GLYPHS_CACHE_MAX_SIZE = 5000;

	/** Memory Cache*/
	private static Cache glyphCache;
	private static int listFontSizes[];
	private VertexArrayList currentGlyphVAL;

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

		/** Memory Cache*/
		glyphCache = new Cache(new CacheConfiguration("roughGlyphsCache",GLYPHS_CACHE_MAX_SIZE).eternal(true).memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU));
		glyphCache.initialise();
	}


	/** Reset memory cache, calling removeAll() Cache method */
	public static void resetCache(){
		glyphCache.removeAll();
	}

	public int getNearestAboveFont(int reqSize_){
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
	}

	public void render(GLAutoDrawable drawable, GlyphVector gV, double scale) {
		int i;
		GL2 gl = drawable.getGL().getGL2();
		for (i = 0; i < gV.getNumGlyphs(); i++) {
			currentGlyphVAL = null;
			if (installGlyph(drawable, gV, i) ) {
				//DRAW A CHARACTER
				//i.e.: each VertexArrayList (many VertexArrays) corresponds to
				//a GlyphVector (many glyphs) and each vertexArray corresponds to
				//a polygon composing one glyph (a char may be composed of many glyphs)
				gl.glPushMatrix();
				gl.glScaled(scale, scale, 1.0);
				for (int j = 0; j < currentGlyphVAL.size(); j++){
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
		//get VertexArray list of the glyph g
		GlyphKey tempKey_ = new GlyphKey( gV.getFont(), gV.getGlyphCode(i_) );
		Element e_ = glyphCache.get(tempKey_);
		if(e_==null){
			temp_offset_x = (float)gV.getGlyphPosition(i_).getX();
			currentGlyphVAL = getTesselation(drawable, gV, i_, temp_offset_x);
			glyphCache.put(new Element(tempKey_, currentGlyphVAL));
		}
		else{
			currentGlyphVAL = (VertexArrayList)e_.getValue();
		}
		return true;
	}

	//We will treat glyph by glyph instead of biting a whole glyphVector
	public VertexArrayList getTesselation(GLAutoDrawable drawable, GlyphVector gV, int i_, float offset_x) {
		//use the x offset so that the origin of all fonts geometry be "0"
		Shape s = gV.getGlyphOutline(i_, -offset_x, 0.0f);
		VertexArrayList tempGlyphVAL = new VertexArrayList();
		VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(tempGlyphVAL);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
		return tempGlyphVAL;
	}

	//MUST implement these abstract methods since we inherit from BasicOutlineFontRender
	protected boolean addTesselation(GLAutoDrawable drawable, int c){System.out.println("Warning! Empty addTesselation() being called.");return false;}
	public boolean installFont(GLAutoDrawable drawable, Font font_, double scale, boolean aa, boolean ufm){System.out.println("Warning! Empty installFont() being called.");return false;}
	public void render(GLAutoDrawable drawable, String string, double scale, Font font_) {System.out.println("Warning! Empty render() being called.");return;}
}
