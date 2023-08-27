/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d;

import agile2d.geom.VertexArray;
import agile2d.geom.VertexArrayList;

/**
 * Tesselates a shape and stores the result in a VertexArrayList
 */
public class VertexArrayTesselatorVisitor implements TesselatorVisitor {
    VertexArrayList list;
    VertexArray last;

    VertexArrayTesselatorVisitor(VertexArrayList list) {
        this.list = list;
    }

    /**
     * @see agile2d.TesselatorVisitor#begin(int)
     */
    public void begin(int mode) {
        VertexArray v = new VertexArray();
        v.setMode(mode);
        list.add(v);
        last = v;
    }

    /**
     * @see agile2d.TesselatorVisitor#addVertex(double[])
     */
    public void addVertex(double[] coords) {
        last.addVertex(coords);
    }

    /**
     * @see agile2d.TesselatorVisitor#addVertex(double, double)
     */
    public void addVertex(double x, double y) {
        last.addVertex(x, y);
    }

    /**
     * @see agile2d.TesselatorVisitor#end()
     */
    public void end() {
        last = null;
    }

    // TesselatorVisitor
    public void combine(double coords[/*3*/], Object data[/*4xn*/],  float weight[/*4*/], Object[/*3*/] dataOut) {
        Tesselator.defaultCombine(coords, data, weight, dataOut);
    }

    public void error(int errorCode) {
        Tesselator.defaultError(errorCode);
    }
}
