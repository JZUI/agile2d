/*
 * Copyright (C) 2006 Jon Meyer, Ben Bederson and Jean-Daniel Fekete               *
 * Copyright (C) 2012 Rodrigo de Almeida, Jean-Daniel Fekete and Emmanuel Pietriga *
 *
 * SPDX-License-Identifier: BSD-4-Clause
 */

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

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.opengl.util.Animator;

import agile2d.AgileGraphics2D;

public class BenchmarkGUI implements ActionListener, ChangeListener, Runnable{
    public final static int CANVAS_W = 640;
    public final static int CANVAS_H = 480;

    private final static int GLCANVAS_TYPE = 0;
    private final static int GLJPANEL_TYPE = 1;
    private final static int NEWT_TYPE = 2;
    private final static int JFRAME_TYPE = 3;
    private Thread thread;
    private AnimeBenchmark benchRef=null;

    static JFrame benchFrame;
    static Animator animator;

    AgileFrame agile;
    GLJPanel glPanel;
    GLCanvas glCanvas;
    NewtCanvasAWT newtCanvas;
    GLWindow glWindow;
    G2DFrame simplePanel;
    JPanel mainPanel, leftPanel, topPanel, radioPanel, canvasRadioPanel;
    JSlider sliderFFamilies, sliderFFRepeat, sliderRects, sliderEmptyOvals, sliderFilledOvals, sliderImages;
    JRadioButton bestStrButton, roughStrButton;
    JRadioButton gljBut, glcBut, newtBut, jfBut;
    ButtonGroup strGroup, canvasGroup;
    JLabel fpsLabel;

    int currentCanvas = GLJPANEL_TYPE;
    int false_counter=0;

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
        this.bestStrButton.setEnabled(b_);
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
        else if(this.currentCanvas==NEWT_TYPE){
            mainPanel.remove(newtCanvas);
            glWindow.removeGLEventListener(agile);
            animator.remove(glWindow);
            agile.dispose(glWindow);
            newtCanvas.destroy();
            newtCanvas=null;
            agile=null;
        }
        else if(this.currentCanvas==JFRAME_TYPE){
            mainPanel.remove(simplePanel);
            simplePanel.stop();
            simplePanel=null;
        }
    }


    private void loadCanvas(int newCanvas_){

        if(newCanvas_==GLJPANEL_TYPE || newCanvas_==GLCANVAS_TYPE || newCanvas_==NEWT_TYPE){
            //Prepare creation of viewPanel (GLView)
            agile = new AgileFrame();
            GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
            caps.setDoubleBuffered(true);// request double buffer display mode
            caps.setSampleBuffers(true);
            caps.setNumSamples(AgileFrame.NB_OF_SAMPLES_FOR_MULTISAMPLE);
            if(newCanvas_==GLJPANEL_TYPE){
                glPanel = new GLJPanel(caps);
                mainPanel.add(glPanel);
                glPanel.addGLEventListener(agile);
                animator = new Animator(glPanel);
                animator.add(glPanel);
                glPanel.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
                glPanel.setBorder(BorderFactory.createLoweredBevelBorder());
            }
            else if (newCanvas_==GLCANVAS_TYPE){
                glCanvas = new GLCanvas(caps);
                mainPanel.add(glCanvas);
                glCanvas.addGLEventListener(agile);

                animator = new Animator(glCanvas);
                animator.add(glCanvas);
                glCanvas.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
            }
            else if (newCanvas_==NEWT_TYPE){
                glWindow = GLWindow.create(caps);
                glWindow.addGLEventListener(agile);
                animator = new Animator(glWindow);
                newtCanvas = new NewtCanvasAWT(glWindow);
                mainPanel.add(newtCanvas);
                newtCanvas.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
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

        loadCanvas(currentCanvas);

        addWidgets();
        thread.start();
    }

    private void addWidgets(){

        //Strategy group buttons
        strGroup = new ButtonGroup();
        bestStrButton = new JRadioButton("Best Text Render Strategy");
        bestStrButton.setActionCommand("BestRender");
        if(agile.getStrategy()==AgileGraphics2D.BEST_TEXT_RENDERING_STRATEGY)
            bestStrButton.setSelected(true);

        roughStrButton = new JRadioButton("Rough Text Render Strategy");
        roughStrButton.setActionCommand("RoughRender");
        if(agile.getStrategy()==AgileGraphics2D.ROUGH_TEXT_RENDERING_STRATEGY)
            roughStrButton.setSelected(true);

        //Group the radio buttons.
        strGroup.add(bestStrButton);
        strGroup.add(roughStrButton);

        //Register a listener for the radio buttons.
        bestStrButton.addActionListener(this);
        roughStrButton.addActionListener(this);

        radioPanel.add(bestStrButton);
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

        newtBut = new JRadioButton("NEWT");
        newtBut.setActionCommand("NEWT");
        if(currentCanvas==NEWT_TYPE)
            newtBut.setSelected(true);

        jfBut = new JRadioButton("JPanel");
        jfBut.setActionCommand("JFrame");
        if(currentCanvas==JFRAME_TYPE)
            jfBut.setSelected(true);

        //Group the radio buttons.
        canvasGroup.add(gljBut);
        canvasGroup.add(glcBut);
        canvasGroup.add(newtBut);
        canvasGroup.add(jfBut);

        //Register a listener for the radio buttons.
        gljBut.addActionListener(this);
        glcBut.addActionListener(this);
        newtBut.addActionListener(this);
        jfBut.addActionListener(this);

        canvasRadioPanel.add(gljBut);
        canvasRadioPanel.add(glcBut);
        canvasRadioPanel.add(newtBut);
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
            agile.setStrategy(AgileGraphics2D.ROUGH_TEXT_RENDERING_STRATEGY);
        }
        else if ("BestRender".equals(e.getActionCommand())) {
            agile.setStrategy(AgileGraphics2D.BEST_TEXT_RENDERING_STRATEGY);
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
        else if ("NEWT".equals(e.getActionCommand())) {
            removeCurrentCanvas();
            currentCanvas=NEWT_TYPE;
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
        //In order to avoid "java.util.zip.ZipException" error messages,
        //set the property below on the head of a static block or on the head of main() call
        //Further details: http://jogamp.org/deployment/jogamp-next/javadoc/gluegen/javadoc/com/jogamp/common/os/Platform.html#USE_TEMP_JAR_CACHE
        System.setProperty("jogamp.gluegen.UseTempJarCache","false");
        startGUI();
    }
}
