/*****************************************************************************
 * Copyright (C) 2006, Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the BSD Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-agile2d.txt file.                                                 *
 *****************************************************************************/

package agile2d;

import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import sun.text.IntHashtable;
import java.util.Hashtable;


/**
 * <code>AgileState</code> locally keeps OpenGL state to avoid useless
 * state changes that are very expensive in OpenGL.
 *
 * <p>Note: AgileGraphics2D uses AgileState to manage all state changes. To interact well with
 * AgileGraphics2D, use AgileState to manage state, or use the AgileGraphics2D.run() method to run code
 * in a safe context. e.g.
 * <pre>
 *    AgileState agileState = AgileState.get(gl);
 *    ...
 *    agileState.glEnable(GL.GL_TEXTURE_2D);
 *    agileState.bindTexture2D(0);
 *    if (agileState.setState(GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE)) {
 *          gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
 *    }
 *    agileState.glDisable(GL.GL_TEXTURE_2D);
 * </pre>
 *
 * @author Jean-Daniel Fekete
 * @version $Revision: 1.3 $
 */
public class AgileState {
    IntHashtable state;
    Hashtable extensions;
    GL2 gl;
    String version;
    int savedCount;
    int doneCount;
    int maxTexSize=0;
    static int[] tmpValue = { 0 };
    static final HashMap<GL2,AgileState> gl2state = new HashMap<GL2, AgileState>();
    static byte lastR, lastG, lastB, lastA;
    static final boolean DEBUG = false;


    /**
     * Get/Create a <code>AgileState</code> object for a GL context.
     *
     * @param gl the GL context
     * @return the associated AgileState
     */
    public static synchronized AgileState get(GL2 gl) {
        AgileState s = gl2state.get(gl);
        if (s == null) {
            s = new AgileState(gl);
			gl2state.put(gl, s); // JM - Moved this up from the Constructor to make it clearer whats going on
		}
        return s;
    }

    /**
     * AgileState constructor.
     *
     * @param gl the GL context.
     */
    protected AgileState(GL2 gl) {
        this.gl = gl;
        state = new IntHashtable();
        state.setDefaultValue(-1);
        initialize();
    }

    private void initialize() {
        version = gl.glGetString(GL.GL_VERSION);
        state.put(GL2.GL_TEXTURE_1D, 0);
        state.put(GL2.GL_TEXTURE_2D, 0);
        state.put(GL2.GL_TEXTURE_BINDING_1D, 0);
        state.put(GL2.GL_TEXTURE_BINDING_2D, 0);
        state.put(GL2.GL_TEXTURE_ENV_MODE, 0);
	getGlExtensions();
    }

    private int initializeState(int attrib) {
        gl.glGetIntegerv(attrib, tmpValue, 0);
        state.put(attrib, tmpValue[0]);
        return tmpValue[0];
    }

    //Gets (only once) a list of GL extensions available on the implementation being used,
    //splits it, and put GL extension strings in a hash table
    private void getGlExtensions(){
	String[] ogl_extensions;
	int table_size;
	ogl_extensions = (gl.glGetString(GL2.GL_EXTENSIONS)).split(" ");
	table_size = ogl_extensions.length;
	extensions = new Hashtable(table_size);
	for(int i=0; i<table_size; i++)
		extensions.put(ogl_extensions[i], 1);
    }

    /**
     * Checks if a given GL Extension is avaiable.
     * @checks if a given GL Extension is avaiable.
     */
    public boolean checkGlExtension(String extensionName){
	if(extensions.containsKey(extensionName)){
//		System.out.println("Extension "+extensionName+" is avaiable");
		return true;
	}
	else{
		System.out.println("GL Extension \""+extensionName+"\" is NOT avaiable");
		return false;
	}
    }

    /**
     * Returns a String representing the version of the OpenGL implementation.
     * @return a String representing the version of the OpenGL implementation.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the GL context.
     *
     * @return the GL context.
     */
    public GL2 getGL() {
        return gl;
    }

    /**
     * Returns the value currently associated with the specified GL attribute.
     * If the constant has not been queried before, the GL state is queried
     * first to initialize the local state to the right value.
     *
     * @param attrib the GL attribute
     * @return the associated value.
     */
    public int getState(int attrib) {
        int s = state.get(attrib);
        if (s == -1) {
            s = initializeState(attrib);
        }
        return s;
    }

    /**
     * Sets the value currently associated with a specified GL attribute.
     * Returns <code>true</code> if the value is different from the one
     * in the GL state, meaning that a GL primitive should be used to set it.

     * * @param attrib the attribute
     * @param value the value r
     * @return <code>true</code> if the value is different from the one
     * in the GL state, meaning that a GL primitive should be used to set it.
     */
    public boolean setState(int attrib, int value) {
        if (getState(attrib) == value) {
            savedCount++;
            return DEBUG;
        }
        doneCount++;
        state.put(attrib, value);
        return true;
    }

    protected void checkError() {
        if (gl.glGetError() != 0)
            System.err.println("Error");
    }

    /**
     * Equivalent to glEnable but checks the value first and skip the
     * GL function is the value is already set to 1.
     * @param attrib the attribute to set.
     */
    public void glEnable(int attrib) {
        if (setState(attrib, 1)) {
            gl.glEnable(attrib);
            checkError();
        }
    }

    /**
     * Equivalent to glDisable but checks the value first and skip the
     * GL function is the value is already set to 0.
     * @param attrib the attribute to set.
     */
    public void glDisable(int attrib) {
        if (setState(attrib, 0)) {
            gl.glDisable(attrib);
            checkError();
        }
    }

    /**
     * Binds the specified texture1D
     * @param tex the texture
     */
    public void bindTexture1D(int tex) {
        if (setState(GL2.GL_TEXTURE_BINDING_1D, tex)) {
            gl.glBindTexture(GL2.GL_TEXTURE_1D, tex);
            checkError();
        }
    }

    /**
     * Binds the specified 2D texture
     * @param tex the texture
     */
    public void bindTexture2D(int tex) {
        if (setState(GL2.GL_TEXTURE_BINDING_2D, tex)) {
            gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
            checkError();
        }
    }

    /**
     * Sets the shade model
     * @param model the new model
     */
    public void glSetShadeModel(int model) {
        if (setState(GL2.GL_SHADE_MODEL, model)) {
            gl.glShadeModel(model);
            checkError();
        }
    }

    /**
     * Enables the specified client state
     * @param mode state to set
     */
    public void glEnableClientState(int mode) {
        if (setState(mode, 1)) {
            gl.glEnableClientState(mode);
            checkError();
        }
    }

    /**
     * Disables the specified client state.
     * @param mode state to disable
     */
    public void glDisableClientState(int mode) {
        if (setState(mode, 0)) {
            gl.glDisableClientState(mode);
            checkError();
        }
    }

    /**
     * Sets the specified logic op
     * @param op the op
     */
    public void glLogicOp(int op) {
        if (setState(GL2.GL_LOGIC_OP, op)) {
            gl.glLogicOp(op);
            checkError();
        }
    }

    /**
     * Sets the texture color
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     */
    public void glColor4ub(byte r, byte g, byte b, byte a) {
        if (lastR != r || lastG != g || lastB != b || lastA != a) {
            lastR = r;
            lastG = g;
            lastB = b;
            lastA = a;
            gl.glColor4ub(r, g, b, a);
            checkError();
        }
    }

    /**
     * Sets the texture color
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     */
    public void glColor4f(float r, float g, float b, float a) {
        glColor4ub((byte)(r*255), (byte)(g*255), (byte)(b*255), (byte)(a*255));
    }


	/**
	 * Pushes all GL attributes onto the GL state stack, using glPushAttrib and glPushClientAttrib.
	 */
	public void save() {
		// Save all attributes
		gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);

		gl.glPushClientAttrib((int)GL2.GL_ALL_CLIENT_ATTRIB_BITS);
	}

	/**
	 * Pops all GL attributes from the GL state stack, using glPopAttrib and glPopClientAttrib.
	 */
	public void restore() {
		// Restore attributes
		gl.glPopClientAttrib();
		gl.glPopAttrib();
	}
}
