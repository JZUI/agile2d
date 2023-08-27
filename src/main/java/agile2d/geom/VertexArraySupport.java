/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete        *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d.geom;

//
// Interface for rendering VertexArrays. Implemented by AgileGraphics2D.
//
public interface VertexArraySupport
{
    public void drawVertexArray(VertexArray array, VertexAttributes attributes);
    public void fillVertexArray(VertexArray array, VertexAttributes attributes);
}
