package agile2d.examples;

/*



import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
*/
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Font;

import javax.media.opengl.*;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.awt.NewtCanvasAWT;


public class HelloWorldNEWT {
	public static int WIN_W = 800;
	public static int WIN_H = 640;
	private static GLCapabilities caps;

	static{
		WIN_W = 800;
		WIN_H = 640;
        GLProfile glp = GLProfile.get(GLProfile.GL2);
        caps = new GLCapabilities(glp);
	}
	
    public static void main(String[] args) {
        GLWindow window = GLWindow.create(caps);
    	final AgileExample agile = new AgileExample(null);
    	window.setSize(WIN_W, WIN_H);        
        window.setTitle("NEWT Window Test");
        window.addGLEventListener(agile);
        window.setVisible(true);
    }
	public static void drawHelloWorld(Graphics2D g_){
		System.out.println("\n\n\nBegin of Hello World\n\n\n");
		Font font_ = new Font("SansSerif", Font.BOLD, 48);
		g_.setFont(font_);		
		g_.drawString("Hello 2D World!", 200, (HelloWorld.WIN_H/2));				
	}
}