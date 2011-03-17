/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/
package agile2d;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.WeakHashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;


/**
 * Manages the mapping between Java AWT Images and OpenGL Texture objects.
 */
final class ImageManager {
	private GL2 gl;			// GL Context
	private AgileState glState;
	private BufferedImage buf;  // Temporary buffer to use to get pixels for image
	private Graphics      bg;   // Graphics object associated with buf.
	private WeakHashMap resident = new WeakHashMap(); // from image -> pool entry
	private ArrayList toDelete = new ArrayList(); // For flusing images from the cache
	private MediaTracker tracker = new MediaTracker(new Label());
	// When the image is not cache, we reuse the same texture
	private Texture defaultTexture;
	//Holds the status of current OpenGL implementation concerning acception textures
	//whoose dimensions are not power-of-two values
	public boolean texture_non_power_of_two=false;

	private class Entry {
//		int id;
		Texture texture;
		Rectangle bounds;

		Entry(Texture texture, Rectangle bounds) {
			this.texture = texture;
			this.bounds = bounds;
		}

		protected void finalize() {
			synchronized (toDelete) { toDelete.add(texture); }
		}
	}

	//
	// Constructs a texture manager. The buffered image and Graphics
	// objects are passed in from the GLGraphics2D class - they are used
	// as temporary store when an image is loaded into texture memory. 
	//
	ImageManager(GL2 gl, BufferedImage buf, Graphics bg) {
		this.gl = gl;
		this.glState = AgileState.get(gl);
		this.buf = buf;
		this.bg = bg;
		if(this.glState.checkGlExtension("GL_ARB_texture_non_power_of_two")==true)
			texture_non_power_of_two=true;
	}

	// Given a (image+bounds), return a texture object for that image.
	//
	private Entry getTextureEntry(Image image, Rectangle bounds) {
		// Test if texture is already loaded
		synchronized (resident) {
			ArrayList entries;
			entries = (ArrayList)resident.get(image);
			if (entries != null) {
				if (bounds == null) {
					bounds = new Rectangle(0, 0, image.getWidth(null), image.getHeight(null));
				}

				// Search for the correct entry
				for (int i = 0; i < entries.size(); i++) {
					Entry entry = (Entry)entries.get(i);
					if (entry.bounds.equals(bounds)) {
						return entry;
					}
				}
			}
			// No entry yet
		}
		return null;
	}

	public void removeTexture(Image image) {
		synchronized (resident) {
			ArrayList entries = (ArrayList)resident.get(image);
			if (entries != null) {
				// Search for the correct entry
				for (int i = 0; i < entries.size(); i++) {
					Entry entry = (Entry)entries.get(i);
					entry.texture.dispose();
				}
			}
			resident.remove(image);
		}
	}
	
	public Texture getTexture(Image image, Rectangle bounds, boolean immutable) {
		Entry entry = getTextureEntry(image, bounds);

		if (entry != null && immutable) {
			return entry.texture;
		}
            
		return null;
	}
        
	public Texture getDefaultTexture() {
		if (defaultTexture == null) {
			defaultTexture = new Texture(gl, 4, buf.getWidth(), buf.getHeight(), false);
		}
		return defaultTexture;
	}

	public boolean drawImage(BufferedImage image, int x, int y, int w, int h) {
		if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
			glState.glEnable(GL2.GL_BLEND);
			if (w != image.getWidth())
				gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, image.getWidth());
			int[] data = ((DataBufferInt)(image.getRaster().getDataBuffer())).getData();
            IntBuffer buffer = IntBuffer.wrap(data);
			gl.glRasterPos2i(x, y);
			gl.glPixelZoom(1, -1);
			gl.glDrawPixels(w, h,
				GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, buffer);
			gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, 0);
			return true;
		}
		else if (image.getType() == BufferedImage.TYPE_INT_RGB) {
			if (w != image.getWidth())
				gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, image.getWidth());
			int[] data = ((DataBufferInt)(image.getRaster().getDataBuffer())).getData();
            IntBuffer buffer = IntBuffer.wrap(data);
			gl.glRasterPos2i(x, y);
			gl.glPixelZoom(1, -1);
			glState.glDisable(GL2.GL_BLEND);
			gl.glDrawPixels(w, h,
				GL2.GL_BGRA, GL2.GL_UNSIGNED_BYTE, buffer);
			gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, 0);
			return true;
		}
		return false;
	}
	
	//
	// Loads an AWT image into an OpenGL texture
	//
	public Texture findTexture(Image image, Rectangle bounds, boolean immutable,
		boolean forTexturePaint) {
		Entry entry = getTextureEntry(image, bounds);

		// For texturePaint, we need to have the texture filled, i.e.
		// grow the image to fit the whole texture width and height
		// to use OpenGL 2D repeated textures. 
		if (entry != null && immutable) {
			if (! forTexturePaint || (forTexturePaint && entry.texture.isNormalized())){
				return entry.texture;
			}
		}
		
		// System.out.println("LOAD TEXTURE " + e.texture.getSize());
		if (!(image instanceof BufferedImage)) {
			// Force AWT images to load
			tracker.addImage(image, 1);
			try { tracker.waitForID(1); } 
			catch (InterruptedException ie) { }
			tracker.removeImage(image);
		}

		if (bounds == null) {
			bounds = new Rectangle(0, 0, image.getWidth(null), image.getHeight(null));
		}
		
		int x = (int)bounds.getX();
		int y = (int)bounds.getY();
		int width = (int)bounds.getWidth();
		int height = (int)bounds.getHeight();
		//
		int twidth, theight;		
		if(texture_non_power_of_two==true){
			twidth = width;
			theight = height;
		}
		else{
			twidth = forTexturePaint ? ImageUtils.nextPowerOf2(width) : width;
			theight = forTexturePaint ? ImageUtils.nextPowerOf2(height) : height;
		}
		// Create the texture
		Texture texture;
		if (entry != null) {  
		  // was immutable before, now said to be mutable
			texture = entry.texture;
		}
		else if (! immutable && 
			(! forTexturePaint || 
			(twidth == buf.getWidth() && theight == buf.getHeight()))) {
			texture = getDefaultTexture();
		}
		else {
			texture = new Texture(gl, 4, twidth, theight, false);
		}

		BufferedImage img =
			(image instanceof BufferedImage) ? ((BufferedImage)image) : null;
		if (img != null && 
			(width == twidth || height == theight) &&
			(img.getType() == BufferedImage.TYPE_INT_ARGB  ||
			img.getType() == BufferedImage.TYPE_INT_RGB)) {
			// we can directly use the image
			texture.loadPixels(img, new Rectangle(x, y, twidth, theight));
		}
		else {
			// We draw the image into a temporary buffer, and then load the 
			// pixels from that buffer. This ensures that the pixel data is accessible
			// in a known format.
			//
			if (!ImageUtils.isOpaque(image)) {
				ImageUtils.clearArea(buf, twidth, theight);
			}
			bg.drawImage(image, 0, 0, twidth, theight, x, y, x+width, y+height, null);
			// Load the pixels into the texture
			texture.loadPixels(buf, new Rectangle(0, 0, twidth, theight));
		}

		// Create and register the entry
		if (immutable && entry == null) {
			entry = new Entry(texture, bounds);
			synchronized (resident) {
				ArrayList entries = (ArrayList)resident.get(image);
				if (entries == null) {
					entries = new ArrayList();
					resident.put(image, entries);
				}
				entries.add(entry);
			}
		}
		
		return texture;
	}

	void flush() {
		synchronized (toDelete) {
			if (toDelete.size() > 0) {
				for (int i = 0; i < toDelete.size(); i++) {
					Texture t = ((Texture)toDelete.get(i));
					t.dispose();
				}
				toDelete.clear();
			}
		}
	}
}
