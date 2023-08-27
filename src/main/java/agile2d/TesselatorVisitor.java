/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d;


/**
 * A TesselatorVisitor abstracts the sending of a list of OpenGL
 * geometry to an OpenGL engine.
 * For old OpenGL implementations, only glBegin and glEnd functions
 * can be used, whereas on more recent versions, Vertex Arrays are
 * much faster since they avoid a function call overhead at each vertex.
 * This is even more true from gl4java that needs to perform a JNI function,
 * more expensive than a mere C function call.
 *
 * TesselatorVisitor objects are meant to be used by Tesselator objects,
 * and for other purposes as well.
 */
interface TesselatorVisitor {
	public void begin(int mode);
	public void addVertex(double[] coords);
	public void addVertex(double x, double y);
	public void end();
	public void error(int errorCode);
	public void combine(double[] coords, Object[] vertex_data, float[] weight, Object[] dataOut);
}
