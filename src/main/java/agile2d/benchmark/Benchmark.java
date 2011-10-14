package agile2d.benchmark;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;

import agile2d.AgileAnimeCanvas;

public class Benchmark {
	private final static int WIN_W = 600;
	private final static int WIN_H = 600;
	private final static int NB_OF_SAMPLES_FOR_MULTISAMPLE = 4;
	
	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out
			.println("\nBad usage.\nYou must pass as an argument the type of component that you want to use: 'GLCanvas' (AWT component) or 'GLJPanel' (Swing component).");
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
			System.exit(0);
		}
		
		final Frame frame = new Frame("Agile2D Demo");
		final AgileAnimeCanvas agile = new AgileAnimeCanvas(null);

		GLCapabilities glCaps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		glCaps.setDoubleBuffered(true);// request double buffer display mode
		glCaps.setSampleBuffers(true);
		glCaps.setNumSamples(NB_OF_SAMPLES_FOR_MULTISAMPLE);

		if (args[0].equals("GLCanvas")) {
			final GLCanvas canvas = new GLCanvas(glCaps);
			frame.add(canvas);
			canvas.addGLEventListener(agile);
			agile.setRoot(canvas);
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
		} else if (args[0].equals("GLJPanel")) {
			final GLJPanel panel = new GLJPanel(glCaps);
			frame.add(panel);
			panel.addGLEventListener(agile);
			agile.setRoot(panel);
		} else {
			System.out
			.println("\nBad usage.\nYou must specify the type of GL component that should be used: 'GLCanvas' (AWT component) or 'GLJPanel' (Swing component).\n");
			System.exit(0);
		}
		frame.setSize(WIN_W, WIN_H);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
		frame.addKeyListener(agile);
		agile.startAnim();
	}	
}
