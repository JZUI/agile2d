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

	//@Override  
	public int compareTo(CharKey other)  
	{  
		return _key.compareTo(other._key);  
	}  

	//@Override  
	public boolean equals(Object other)  
	{  
		return (other != null) && (getClass() == other.getClass()) &&   
		_key.equals(((CharKey)other)._key);  
	}  

	//@Override  
	public int hashCode()  
	{  
		return _key.hashCode();  
	}  

	//@Override  
	public String toString()  
	{  
		return _key;  
	}  

	public int size()  
	{  
		return _size;  
	}

}  


class OutlineRoughFontRenderer extends BasicOutlineFontRenderer {

	private VertexArrayList presentVAL;	
	private SoftHashMap charSoftHashMap;

	//This is the "by default" value of this parameter
	//the user may though control it through a render quality hint
	//That's the role of method FontManager.setRoughOutlineQuality(hint_)
	private static final float FONT_SIZE_INTERVAL_FACTOR = 1.2f;
	private static final int MIN_PRE_RENDER_FONT_SIZE = 18;
	private static final int MAX_PRE_RENDER_FONT_SIZE = 2048;
	private static final int INIT_FONT_SIZE_LENGTH = 64;
	private static final int CHARS_HASHMAP_SIZE = 256;

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
	
	public int getNextUpperSize(int reqSize_){
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

		System.out.println("Scale :"+scale);	
		
		//TODO: Should a glyphvector for all the font char be created or only for the required char / strings ?
		//Create a new glyphvector from the current font
		setup();
		installed = true;
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
			presentVAL = null;
			if (installChar(drawable, c)) {
				//Get the metrics for each character
				GlyphMetrics m = metrics[c];				
				//DRAW A CHARACTER
				//i.e.: each VertexArrayList (many VertexArrays) corresponds to
				//a character (many polygons) and each vertexArray corresponds to
				//a convex polygon composing the character
				gl.glPushMatrix();
				gl.glScaled(scale, scale, 1.0);
				for (int j = 0; j < presentVAL.size(); j++)
					ShapeManager.render(gl, presentVAL.getVertexArrayAt(j), null);
				gl.glPopMatrix();
				gl.glTranslated((m.getAdvanceX())*scale, (m.getAdvanceY())*scale, 0.0d);
			}
		}
		installed = false;
	}


	protected boolean installChar(GLAutoDrawable drawable, int c) {

		GL2 gl = drawable.getGL().getGL2();

		//get VertexArray list of the character c
		CharKey tempKey_ = new CharKey( (char) c, this.font);
		presentVAL = (VertexArrayList)charSoftHashMap.get(tempKey_);

		//If data doesn't exist (still / anymore), make it;
		if (presentVAL == null){
			addTesselation(drawable, c);
			System.out.println("No vertexArray for character: "+tempKey_.toString());
		}
		else
			System.out.println("Found VertexArray for character: "+tempKey_.toString());
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
		presentVAL = new VertexArrayList();
		VertexArrayTesselatorVisitor visitor = new VertexArrayTesselatorVisitor(presentVAL);
		tesselator.tesselate(s.getPathIterator(null, 0.01), visitor);
		CharKey tempCharKey_ = new CharKey((char)charIndex, this.font);
		charSoftHashMap.put((Object)tempCharKey_, (Object)presentVAL);
		return true;
	}

} 
