package agile2d.benchmark;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;

import agile2d.AgileGraphics2D;

import com.jogamp.opengl.util.Animator;

public class BenchmarkGUI implements ActionListener, ChangeListener, Runnable{
	public final static int CANVAS_W = 640;
	public final static int CANVAS_H = 480;
	
	private final static int GLCANVAS_TYPE = 0;
	private final static int GLJPANEL_TYPE = 1;
	private final static int JFRAME_TYPE = 2;
	private Thread thread;
	private AnimeBenchmark benchRef=null;
	private int current_strategy=AgileGraphics2D.DEFAULT_STRATEGY;
	
	AgileFrame agile;
	GLJPanel glPanel; 
	GLCanvas glCanvas;
	G2DFrame simplePanel;
	JPanel mainPanel, leftPanel, topPanel, radioPanel, canvasRadioPanel;
	static Animator animator;
	JSlider sliderFFamilies, sliderFFRepeat, sliderRects, sliderEmptyOvals, sliderFilledOvals, sliderImages;
	JRadioButton defaultStrButton, roughStrButton;
	JRadioButton gljBut, glcBut, jfBut;
	ButtonGroup strGroup, canvasGroup;
	JLabel fpsLabel;
	static JFrame benchFrame;
	int currentCanvas = GLJPANEL_TYPE;
	int false_counter=0;

	static { GLProfile.initSingleton(true); }
	
	public void run() {
		Thread me = Thread.currentThread();
		while (thread == me) {
			if(benchRef!=null)
				this.setFpsLabel(benchRef.getLastFPS());
			else
				benchRef = agile.getRefToBench();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) { break; }
		}
		thread = null;
	}
	
	
	private void enableAgileOptions(boolean b_){			
		this.roughStrButton.setEnabled(b_);
		this.defaultStrButton.setEnabled(b_);
	}
	
	
	private void removeCurrentCanvas(){
		if(this.currentCanvas==GLJPANEL_TYPE){
			mainPanel.remove(glPanel);
			glPanel.removeGLEventListener(agile);
			animator.remove(glPanel);
			agile.dispose(glPanel);
			glPanel=null;
			agile=null;
		}
		else if(this.currentCanvas==GLCANVAS_TYPE){
			mainPanel.remove(glCanvas);
			glCanvas.removeGLEventListener(agile);
			animator.remove(glCanvas);
			agile.dispose(glCanvas);
			glCanvas=null;
			agile=null;
		}
		else if(this.currentCanvas==JFRAME_TYPE){
			mainPanel.remove(simplePanel);
			simplePanel.stop();
			simplePanel=null;
		}		
	}
	

	private void loadCanvas(int newCanvas_){
		current_strategy = AgileGraphics2D.DEFAULT_STRATEGY;
		defaultStrButton.setSelected(true);
		
		if(newCanvas_==GLJPANEL_TYPE || newCanvas_==GLCANVAS_TYPE){
			//Prepare creation of viewPanel (GLView)
			agile = new AgileFrame();
			GLCapabilities glCaps = new GLCapabilities(GLProfile.getDefault()); 
			glCaps.setDoubleBuffered(true);// request double buffer display mode
			glCaps.setSampleBuffers(true);
			glCaps.setNumSamples(AgileFrame.NB_OF_SAMPLES_FOR_MULTISAMPLE);
			if(newCanvas_==GLJPANEL_TYPE){
				glPanel = new GLJPanel(glCaps);
				mainPanel.add(glPanel);
				glPanel.addGLEventListener(agile);
				animator = new Animator(glPanel);
				animator.add(glPanel);
				glPanel.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
				glPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			}	
			else if (newCanvas_==GLCANVAS_TYPE){
				glCanvas = new GLCanvas(glCaps);
				mainPanel.add(glCanvas);
				glCanvas.addGLEventListener(agile);

				animator = new Animator(glCanvas);
				animator.add(glCanvas);
				glCanvas.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));			
			}
			//Start the animator specific to agile (part of JOGL)
			animator.start();
			benchRef=agile.getRefToBench();
			
		}
		else if (newCanvas_==JFRAME_TYPE){
			simplePanel = new G2DFrame();
			mainPanel.add(simplePanel);
			simplePanel.setVisible(true);
			simplePanel.start(); //Start the ordinary animator (not specific to JOGL)
			simplePanel.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
			simplePanel.setBorder(BorderFactory.createLoweredBevelBorder());
			benchRef=simplePanel.getRefToBench();
		}
		mainPanel.invalidate();
	}

	public BenchmarkGUI(){
		thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);		

		//Create panels and widgets
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));        

		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(2, 2));
		leftPanel = new JPanel(new GridLayout(3, 2));
		mainPanel.add(topPanel,BorderLayout.NORTH);
		mainPanel.add(leftPanel,BorderLayout.WEST);

		topPanel.setPreferredSize(new Dimension(CANVAS_W, 100));
		leftPanel.setPreferredSize(new Dimension(200, CANVAS_H-200));

		//Label
		fpsLabel = new JLabel("Initializing...");
		//Radio buttons
		radioPanel = new JPanel(new GridLayout(1, 2));
		radioPanel.setBorder(BorderFactory.createEtchedBorder());
		canvasRadioPanel = new JPanel(new GridLayout(1, 3));
		canvasRadioPanel.setBorder(BorderFactory.createEtchedBorder());
		//Sliders
		sliderFFamilies = new JSlider(JSlider.VERTICAL, 0, AnimeBenchmark.MAX_NB_FONTS, 1);
		sliderFFRepeat = new JSlider(JSlider.VERTICAL, 1, AnimeBenchmark.MAX_NB_REPETITIONS, 1);
		sliderRects = new JSlider(JSlider.VERTICAL, 0, AnimeBenchmark.MAX_NB_SHAPES/AnimeBenchmark.NB_SHAPE_TYPES, 0);
		sliderEmptyOvals = new JSlider(JSlider.VERTICAL, 0, AnimeBenchmark.MAX_NB_SHAPES/AnimeBenchmark.NB_SHAPE_TYPES, 0);
		sliderFilledOvals = new JSlider(JSlider.VERTICAL, 0, AnimeBenchmark.MAX_NB_SHAPES/AnimeBenchmark.NB_SHAPE_TYPES, 0);
		sliderImages = new JSlider(JSlider.VERTICAL, 0, AnimeBenchmark.MAX_NB_IMAGES, 0);
		
		//TOP PANEL
		topPanel.add(fpsLabel);
		topPanel.add(new JLabel());
		topPanel.add(canvasRadioPanel);
		topPanel.add(radioPanel);
		//
		//LEFT PANEL
		leftPanel.add(sliderFFamilies);
		leftPanel.add(sliderFFRepeat);
		leftPanel.add(sliderEmptyOvals);
		leftPanel.add(sliderFilledOvals);
		leftPanel.add(sliderRects);
		leftPanel.add(sliderImages);

		addWidgets();
		loadCanvas(currentCanvas);
		thread.start();
	}

	private void addWidgets(){

		//Strategy group buttons
		strGroup = new ButtonGroup();
		defaultStrButton = new JRadioButton("Default render strategy");
		defaultStrButton.setActionCommand("DefaultRender");
		if(current_strategy==AgileGraphics2D.DEFAULT_STRATEGY)
			defaultStrButton.setSelected(true);

		roughStrButton = new JRadioButton("Rough render strategy");
		roughStrButton.setActionCommand("RoughRender");
		if(current_strategy==AgileGraphics2D.ROUGH_SCALE_STRATEGY)
			roughStrButton.setSelected(true);

		//Group the radio buttons.
		strGroup.add(defaultStrButton);
		strGroup.add(roughStrButton);

		//Register a listener for the radio buttons.
		defaultStrButton.addActionListener(this);
		roughStrButton.addActionListener(this);

		radioPanel.add(defaultStrButton);
		radioPanel.add(roughStrButton);

		//Canvas group buttons
		canvasGroup = new ButtonGroup();
		gljBut = new JRadioButton("GLJPanel");
		gljBut.setActionCommand("GLJPanel");
		if(currentCanvas==GLJPANEL_TYPE)
			gljBut.setSelected(true);
		
		glcBut = new JRadioButton("GLCanvas");
		glcBut.setActionCommand("GLCanvas");
		if(currentCanvas==GLCANVAS_TYPE)
			glcBut.setSelected(true);

		jfBut = new JRadioButton("Standard JFrame");
		jfBut.setActionCommand("JFrame");
		if(currentCanvas==JFRAME_TYPE)
			jfBut.setSelected(true);		
		
		//Group the radio buttons.
		canvasGroup.add(gljBut);
		canvasGroup.add(glcBut);
		canvasGroup.add(jfBut);

		//Register a listener for the radio buttons.
		gljBut.addActionListener(this);
		glcBut.addActionListener(this);
		jfBut.addActionListener(this);

		canvasRadioPanel.add(gljBut);
		canvasRadioPanel.add(glcBut);
		canvasRadioPanel.add(jfBut);		

		//SLiders
		sliderFFamilies.setBorder(BorderFactory.createTitledBorder("Fonts"));
		sliderFFamilies.setName("FontNumber");
		sliderFFamilies.addChangeListener(this);
		sliderFFamilies.setMajorTickSpacing(1);
		sliderFFamilies.setPaintTicks(true);
		sliderFFamilies.setSnapToTicks(true);
		sliderFFamilies.setPaintLabels(true);

		sliderFFRepeat.setBorder(BorderFactory.createTitledBorder("Repeat Font"));
		sliderFFRepeat.setName("Repetitions");    
		sliderFFRepeat.addChangeListener(this);
		sliderFFRepeat.setMajorTickSpacing(1);
		sliderFFRepeat.setPaintTicks(true);
		sliderFFRepeat.setSnapToTicks(true);
		sliderFFRepeat.setPaintLabels(true);

		sliderEmptyOvals.setBorder(BorderFactory.createTitledBorder("Empty Ovals"));
		sliderEmptyOvals.setName("EmptyOvals");    
		sliderEmptyOvals.addChangeListener(this);
		sliderEmptyOvals.setMajorTickSpacing(AnimeBenchmark.tick_interval);
		sliderEmptyOvals.setPaintTicks(true);
		sliderEmptyOvals.setPaintLabels(true);		

		sliderFilledOvals.setBorder(BorderFactory.createTitledBorder("Full Ovals"));
		sliderFilledOvals.setName("FilledOvals");    
		sliderFilledOvals.addChangeListener(this);
		sliderFilledOvals.setMajorTickSpacing(AnimeBenchmark.tick_interval);
		sliderFilledOvals.setPaintTicks(true);
		sliderFilledOvals.setPaintLabels(true);		

		sliderRects.setBorder(BorderFactory.createTitledBorder("Rectangles"));
		sliderRects.setName("Rects");    
		sliderRects.addChangeListener(this);
		sliderRects.setMajorTickSpacing(AnimeBenchmark.tick_interval);
		sliderRects.setPaintTicks(true);
		sliderRects.setPaintLabels(true);

		sliderImages.setBorder(BorderFactory.createTitledBorder("Images"));
		sliderImages.setName("Images");    
		sliderImages.addChangeListener(this);
		sliderImages.setMajorTickSpacing(1);
		sliderImages.setPaintTicks(true);
		sliderImages.setPaintLabels(true);
		sliderImages.setSnapToTicks(true);
}

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		if (!source.getValueIsAdjusting()) {
			if(source.getName().equals("Repetitions")){
				AnimeBenchmark.setNbRepetitions((int)source.getValue());
			}
			else if(source.getName().equals("FontNumber")){
				AnimeBenchmark.setNbFonts((int)source.getValue());
			}			
			else if(source.getName().equals("EmptyOvals")){
				AnimeBenchmark.setNbEmptyOvals((int)source.getValue());
			}				
			else if(source.getName().equals("FilledOvals")){
				AnimeBenchmark.setNbFullOvals((int)source.getValue());
			}
			else if(source.getName().equals("Rects")){
				AnimeBenchmark.setNbRects((int)source.getValue());
			}
			else if(source.getName().equals("Images")){
				AnimeBenchmark.setNbImages((int)source.getValue());
			}	
		}
	}


	public void setFpsLabel(int lastFPS_){
		fpsLabel.setText("FPS: "+lastFPS_);
	}


	public void actionPerformed(ActionEvent e) {
		if ("RoughRender".equals(e.getActionCommand())) {
			agile.setStrategy(AgileGraphics2D.ROUGH_SCALE_STRATEGY);
		}
		else if ("DefaultRender".equals(e.getActionCommand())) {
			agile.setStrategy(AgileGraphics2D.DEFAULT_STRATEGY);
		}
		else if ("GLJPanel".equals(e.getActionCommand())) {
			removeCurrentCanvas();
			currentCanvas=GLJPANEL_TYPE;
			this.loadCanvas(currentCanvas);
			enableAgileOptions(true);
			benchFrame.setVisible(true);
		}
		else if ("GLCanvas".equals(e.getActionCommand())) {
			
			removeCurrentCanvas();
			currentCanvas=GLCANVAS_TYPE;			
			this.loadCanvas(currentCanvas);
			enableAgileOptions(true);
			benchFrame.setVisible(true);
		}
		else if ("JFrame".equals(e.getActionCommand())) {
			removeCurrentCanvas();
			currentCanvas=JFRAME_TYPE;	
			this.loadCanvas(currentCanvas);
			enableAgileOptions(false);
			benchFrame.setVisible(true);
		}

	} 

	private static void startGUI(){
		JFrame.setDefaultLookAndFeelDecorated(true);
		//Frame settings
		BenchmarkGUI bench = new BenchmarkGUI();
		benchFrame = new JFrame("Agile2D Benchmark");
		benchFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		benchFrame.setContentPane(bench.mainPanel);
		benchFrame.setSize(AnimeBenchmark.WIN_W, AnimeBenchmark.WIN_H);
		//
		benchFrame.setVisible(true); 
	}

	public static void main(String[] args) {
		startGUI();

	}
}
