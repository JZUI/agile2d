/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d;

/**
 * Default implementation of a TesselatorVisitor.
 *
 * @author Jean-Daniel Fekete
 * @version $Revision: 1.1 $
 */
public class TesselatorAdapter implements TesselatorVisitor {
    /**
     * {@inheritDoc}
     */
    public void begin(int mode) {
    }

    /**
     * {@inheritDoc}
     */
    public void addVertex(double[] coords) {
    }

public void addVertex(double x, double y){
    }

    /**
     * {@inheritDoc}
     */
    public void end() {
    }

    /**
     * {@inheritDoc}
     */
    public void error(int errorCode) {
        Tesselator.defaultError(errorCode);
    }

    /**
     * {@inheritDoc}
     */

    public void combine(
            double[] coords,
            Object[] vertex_data,//double[] vertex_data,
            float[] weight,
        Object[] dataOut) { //double[] dataOut) {
        Tesselator.defaultCombine(coords, vertex_data, weight, dataOut);
    }

}
