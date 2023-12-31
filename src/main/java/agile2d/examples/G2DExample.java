/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d.examples;

import java.awt.Color;

import java.awt.Graphics;
import java.awt.Graphics2D;


import javax.swing.*;

/**
 * <b>Hello World Example</b>
 *
 */
public class G2DExample extends JFrame {
    private static Graphics2D g2;

    public void paint(Graphics g) {
        // Paint sample primitives
        g2 = (Graphics2D)g;
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, HelloWorld.WIN_W, HelloWorld.WIN_H);
        HelloWorld.drawHelloWorld(g2);
    }
}
