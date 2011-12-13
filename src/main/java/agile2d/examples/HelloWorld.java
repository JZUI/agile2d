package agile2d.examples;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;

//import com.jogamp.newt.event.WindowAdapter;
//import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.awt.NewtCanvasAWT;


public class HelloWorld {
	public final static int WIN_W = 800;
	public final static int WIN_H = 640;

	private static final String ARG_ERROR = "Bad usage.\nYou must pass as an argument the type of component that you want to use: 'GLCanvas' (Agile2D with AWT component) or 'GLJPanel' (Agile2D with Swing component) or 'NEWT' (Agile2D with NEWT component) or 'JFrame' (pure Graphics2D implementation, i.e., no Agile2D)";

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("\n"+ARG_ERROR);
			System.exit(0);
		}

		if(args[0].equals("GLCanvas") || args[0].equals("GLJPanel") || args[0].equals("NEWT")){
			final Frame frame = new Frame("Agile2D Demo");
			final AgileExample agile = new AgileExample(null);

			GLProfile myGLProfile = GLProfile.get(GLProfile.GL2);
			GLCapabilities caps = new GLCapabilities(myGLProfile);
			caps.setDoubleBuffered(true);// request double buffer display mode
			caps.setSampleBuffers(true);
			caps.setNumSamples(AgileExample.NB_OF_SAMPLES_FOR_MULTISAMPLE);

			if (args[0].equals("GLCanvas")) {
				final GLCanvas canvas = new GLCanvas(caps);
				frame.add(canvas);
				canvas.addGLEventListener(agile);
				agile.setRoot(canvas);
			} else if (args[0].equals("GLJPanel")) {
				final GLJPanel panel = new GLJPanel(caps);
				frame.add(panel);
				panel.addGLEventListener(agile);
				agile.setRoot(panel);
			}
			else if (args[0].equals("NEWT")) {
				GLWindow window = GLWindow.create(caps); 
				window.addGLEventListener(agile);        
				NewtCanvasAWT canvas = new NewtCanvasAWT(window);
				frame.add(canvas);
			}
			//Frame settings
			frame.setSize(HelloWorld.WIN_W, HelloWorld.WIN_H);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			frame.setVisible(true);			
			frame.addKeyListener(agile);
		}
		else if (args[0].equals("JFrame")) {
			G2DExample frame = new G2DExample();
			frame.setSize(HelloWorld.WIN_W, HelloWorld.WIN_H);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			frame.setVisible(true);
		} else {
			System.out.println("\n"+ARG_ERROR);
			System.exit(0);
		}
	}

	public static void drawHelloWorld(Graphics2D g_){
		Font font_ = new Font("SansSerif", Font.BOLD, 48);
		g_.setFont(font_);		
		g_.drawString("Hello 2D World!", 200, (HelloWorld.WIN_H/2));				
	}
}
