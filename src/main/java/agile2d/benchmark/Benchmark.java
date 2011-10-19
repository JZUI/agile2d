package agile2d.benchmark;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import com.sun.opengl.util.Animator;

public class Benchmark {

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out
			.println("\nBad usage.\nYou must pass as an argument the type of component that you want to use: 'GLCanvas' (AWT component) or 'GLJPanel' (Swing component).");
			System.out
			.println("Observation: 'GLJPanel' enables antialiasing thru multisampling.\n\n");
			System.exit(0);
		}

		if(args[0].equals("GLCanvas") || args[0].equals("GLJPanel") ){
			final Frame frame = new Frame("Agile2D Demo");
			final AgileFrame agile = new AgileFrame(null);
			
			GLCapabilities glCaps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
			glCaps.setDoubleBuffered(true);// request double buffer display mode
			glCaps.setSampleBuffers(true);
			glCaps.setNumSamples(AgileFrame.NB_OF_SAMPLES_FOR_MULTISAMPLE);
			Animator animator;
			
			if (args[0].equals("GLCanvas")) {
				final GLCanvas canvas = new GLCanvas(glCaps);
				frame.add(canvas);
				canvas.addGLEventListener(agile);
				agile.setRoot(canvas);
				
				animator = new Animator(canvas);
				animator.add(canvas);
				frame.setVisible(true);
				animator.start();
				
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
				
				animator = new Animator(panel);
				animator.add(panel);
				frame.setVisible(true);
				animator.start();
			}
			//Frame settings
			frame.setSize(AnimeBenchmark.WIN_W, AnimeBenchmark.WIN_H);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});	
			frame.addKeyListener(agile);
			//agile.startAnim();
		}
		else if (args[0].equals("JFrame")) {
			final G2DFrame frame = new G2DFrame();
			frame.setSize(AnimeBenchmark.WIN_W, AnimeBenchmark.WIN_H);
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
				frame.setVisible(true);
				frame.start();
		} else {
			System.out
			.println("\nBad usage.\nYou must specify the type of GL component that should be used: 'GLCanvas' (AWT component) or 'GLJPanel' (Swing component).\n");
			System.exit(0);
		}
	}	
}
