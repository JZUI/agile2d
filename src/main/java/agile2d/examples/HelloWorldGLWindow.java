/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

package agile2d.examples;

import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Font;

import javax.media.opengl.*;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.awt.NewtCanvasAWT;


public class HelloWorldGLWindow {
    public static int WIN_W = 800;
    public static int WIN_H = 640;


    public static void main(String[] args) {
        //Set this property here to avoid "java.util.zip.ZipException" error messages
        //Further details: http://jogamp.org/deployment/jogamp-next/javadoc/gluegen/javadoc/com/jogamp/common/os/Platform.html#USE_TEMP_JAR_CACHE
        System.setProperty("jogamp.gluegen.UseTempJarCache","false");
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        //
        GLWindow window = GLWindow.create(caps);
        final AgileExample agile = new AgileExample(null);
        window.addGLEventListener(agile);
        NewtCanvasAWT canvas = new NewtCanvasAWT(window);
        Frame frame = new Frame("Agile2D HelloWorld using NEWT Components");
        frame.add(canvas);
        frame.setSize(WIN_W, WIN_H);
        frame.setVisible(true);
        frame.addKeyListener(agile);
    }

    public static void drawHelloWorld(Graphics2D g_){
        System.out.println("\n\n\nBegin of Hello World\n\n\n");
        Font font_ = new Font("SansSerif", Font.BOLD, 48);
        g_.setFont(font_);
        g_.drawString("Hello 2D World!", 200, (HelloWorld.WIN_H/2));
    }
}
