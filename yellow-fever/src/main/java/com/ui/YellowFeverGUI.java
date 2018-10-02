package com.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.JFrame;

//import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialScale;

import com.core.YellowFever;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.inspector.TabbedInspector;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Valuable;
import sim.util.geo.MasonGeometry;
import sim.util.media.chart.TimeSeriesChartGenerator;

public class YellowFeverGUI extends GUIState {

  private Display2D display;
  private JFrame displayFrame;
  private Display2D displayRainfall;
  private JFrame displayFrameRainfall;
  private FastValueGridPortrayal2D rainfallPortrayal = new FastValueGridPortrayal2D();
  private ContinuousPortrayal2D refugeePortrayal = new ContinuousPortrayal2D();
  private SparseGridPortrayal2D facilPortrayal = new SparseGridPortrayal2D();
  private GeomVectorFieldPortrayal roadShapeProtrayal = new GeomVectorFieldPortrayal();
  private GeomVectorFieldPortrayal campShapeProtrayal = new GeomVectorFieldPortrayal();

  private TimeSeriesChartGenerator chartSeriesCholera;
  private TimeSeriesChartGenerator chartSeriesCholeraNewly;
  private TimeSeriesChartGenerator chartSeriesPopulation;

  public static void main(String[] args) {
    YellowFeverGUI dadaabGUI = new YellowFeverGUI(args);
    Console console = new Console(dadaabGUI);
    console.setVisible(true);
  }

  public YellowFeverGUI(String[] args) {
    super(new YellowFever(System.currentTimeMillis(), args));
  }

  public YellowFeverGUI(SimState state) {
    super(state);
  }

  public void start() {
    super.start();
    // set up our portrayals
    setupPortrayals();
  }

  @Override
  public void load(SimState state) {
    super.load(state);
    setupPortrayals();
  }

  public void setupPortrayals() {
    YellowFever dadaab = (YellowFever) state;
    rainfallPortrayal.setField(dadaab.rainfallGrid);
    refugeePortrayal.setField(dadaab.allHumans);

    OvalPortrayal2D rPortrayal = new OvalPortrayal2D(0.20) {
      // to draw each refugee type with differnet color
      public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        if (object != null) {
          double cType = ((Valuable) object).doubleValue();
          if (cType == 1) {
            paint = Color.BLACK; // SUSCEPTIBLE
          } else if (cType == 2) {
            paint = Color.YELLOW; // EXPOSED
          } else if (cType == 3) {
            paint = Color.PINK; // MILD_INFECTION
          } else if (cType == 4) {
            paint = Color.ORANGE; // SEVERE_INFECTION
          } else if (cType == 5) {
            paint = Color.RED; // TOXIC_INFECTION
          } else {
            paint = Color.BLUE; // RECOVERED
          }
          super.draw(object, graphics, info);
        } else {
          super.draw(object, graphics, info);
        }
      }
    };

    refugeePortrayal.setPortrayalForAll(rPortrayal);

    facilPortrayal.setField(dadaab.facilityGrid);
    // facility portrial
    RectanglePortrayal2D facPortrayal = new RectanglePortrayal2D(1.0, false) {
      final Color borehole = new Color(0, 128, 255);
      final Color healthC = new Color(255, 0, 0);
      final Color school = new Color(0, 255, 0);
      final Color foodC = new Color(102, 0, 102);
      final Color mosq = new Color(0, 0, 102);
      final Color market = new Color(0, 102, 102);
      final Color other = new Color(255, 255, 255);

      // to draw each refugee type with differnet color
      public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        if (object != null) {
          double cType = ((Valuable) object).doubleValue();
          if (cType == 1) {
            paint = school;
          } else if (cType == 2) {
            paint = borehole;
          } else if (cType == 3) {
            paint = mosq;
          } else if (cType == 4) {
            paint = market;
          } else if (cType == 5) {
            paint = foodC;
          } else if (cType == 6) {
            paint = healthC;
          } else {
            paint = other;
          }
          super.draw(object, graphics, info);
        } else {
          super.draw(object, graphics, info);
        }
      }
    };

    facilPortrayal.setPortrayalForAll(facPortrayal);
    // camp shape port..
    campShapeProtrayal.setField(dadaab.campShape);

    GeomPortrayal gp = new GeomPortrayal(true) {
      final Color d = new Color(224, 255, 224);
      final Color i = new Color(255, 180, 210);
      final Color h = new Color(204, 204, 153);
      final Color o = new Color(255, 255, 255);

      public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        if (object != null) {
          MasonGeometry mg = (MasonGeometry) object;

          // AttributeValue key = new AttributeValue();
          Integer cType = mg.getIntegerAttribute("CAMPID");
          // int cType = (Integer) cID.get(afterSize);
          if (cType == 1) {
            paint = d;
          } else if (cType == 2) {
            paint = i;
          } else if (cType == 3) {
            paint = h;
          } else {
            paint = o;
          }
          super.draw(object, graphics, info);
        } else {
          super.draw(object, graphics, info);
        }
      }
    };

    campShapeProtrayal.setPortrayalForAll(gp);

    roadShapeProtrayal.setField(dadaab.roadLinks);
    roadShapeProtrayal.setPortrayalForAll(new GeomPortrayal(Color.LIGHT_GRAY, false));

    // roadPortrayal.setField(dadaab.roadGrid);
    // roadPortrayal.setPortrayalForAll(new
    // RectanglePortrayal2D(Color.LIGHT_GRAY));

    display.reset();
    display.setBackdrop(Color.white);
    // redraw the display
    display.repaint();

    displayRainfall.reset();
    displayRainfall.setBackdrop(Color.white);
    // redraw the display
    displayRainfall.repaint();
  }

  public void init(Controller c) {
    super.init(c);
    display = new Display2D(380, 760, this);
    displayRainfall = new Display2D(380, 760, this);

    // display.attach(landPortrayal, "Camps");
    display.attach(campShapeProtrayal, "Camps Vector");
    display.attach(roadShapeProtrayal, "Road Vector");
    display.attach(refugeePortrayal, "Refugees");
    display.attach(facilPortrayal, "Facility");

    // Dadaab db = (Dadaab) state;
    displayFrame = display.createFrame();
    c.registerFrame(displayFrame);
    displayFrame.setVisible(true);

    displayRainfall.attach(rainfallPortrayal, "Rainfall");

    displayFrameRainfall = displayRainfall.createFrame();
    c.registerFrame(displayFrameRainfall);
    displayFrameRainfall.setVisible(false);
    displayFrameRainfall.setTitle("Rainfall");

    // Portray activity chart
    JFreeChart chart = ChartFactory.createBarChart("Refugee's Activity", "Activity", "Percentage",
        ((YellowFever) this.state).dataset, PlotOrientation.VERTICAL, false, false, false);
    chart.setBackgroundPaint(Color.WHITE);
    chart.getTitle().setPaint(Color.BLACK);

    CategoryPlot p = chart.getCategoryPlot();
    p.setBackgroundPaint(Color.WHITE);
    p.setRangeGridlinePaint(Color.red);

    // set the range axis to display integers only...
    NumberAxis rangeAxis = (NumberAxis) p.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    int max = 100; // ((Dadaab) this.state).getInitialRefugeeNumber();
    rangeAxis.setRange(0, max);

    ChartFrame frame = new ChartFrame("Activity Chart", chart);
    frame.setVisible(false);
    frame.setSize(400, 350);

    frame.pack();
    c.registerFrame(frame);

    // Portray activity chart
    JFreeChart agechart = ChartFactory.createBarChart("Age Distribution", "Age  Group",
        "Percentage of Total Population", ((YellowFever) this.state).agedataset, PlotOrientation.VERTICAL, false, false,
        false);
    agechart.setBackgroundPaint(Color.WHITE);
    agechart.getTitle().setPaint(Color.BLACK);

    CategoryPlot pl = agechart.getCategoryPlot();
    pl.setBackgroundPaint(Color.WHITE);
    pl.setRangeGridlinePaint(Color.BLUE);

    // set the range axis to display integers only...
    NumberAxis agerangeAxis = (NumberAxis) pl.getRangeAxis();
    agerangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    ChartFrame ageframe = new ChartFrame("Age Chart", agechart);
    ageframe.setVisible(false);
    ageframe.setSize(400, 350);

    ageframe.pack();
    c.registerFrame(ageframe);

    // Portray activity chart
    JFreeChart famchart = ChartFactory.createBarChart("Household Size", "Size", "Total",
        ((YellowFever) this.state).familydataset, PlotOrientation.VERTICAL, false, false, false);
    famchart.setBackgroundPaint(Color.WHITE);
    famchart.getTitle().setPaint(Color.BLACK);

    CategoryPlot pf = famchart.getCategoryPlot();
    pf.setBackgroundPaint(Color.WHITE);
    pf.setRangeGridlinePaint(Color.BLUE);

    // set the range axis to display integers only...
    NumberAxis famrangeAxis = (NumberAxis) pf.getRangeAxis();
    famrangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    ChartFrame famframe = new ChartFrame("Household Size Chart", famchart);
    famframe.setVisible(false);
    famframe.setSize(400, 350);

    famframe.pack();
    c.registerFrame(famframe);
    //

    Dimension dm = new Dimension(30, 30);
    Dimension dmn = new Dimension(30, 30);

    chartSeriesCholera = new sim.util.media.chart.TimeSeriesChartGenerator();
    chartSeriesCholera.createFrame();
    chartSeriesCholera.setSize(dm);
    chartSeriesCholera.setTitle("Health Status");
    chartSeriesCholera.setRangeAxisLabel("Number of People");
    chartSeriesCholera.setDomainAxisLabel("Minutes");
    chartSeriesCholera.setMaximumSize(dm);
    chartSeriesCholera.setMinimumSize(dmn);
    chartSeriesCholera.addSeries(((YellowFever) this.state).totalsusceptibleSeries, null);
    chartSeriesCholera.addSeries(((YellowFever) this.state).totalExposedSeries, null);
    chartSeriesCholera.addSeries(((YellowFever) this.state).totalInfectedSeries, null);
    chartSeriesCholera.addSeries(((YellowFever) this.state).totalRecoveredSeries, null);
    chartSeriesCholera.addSeries(((YellowFever) this.state).rainfallSeries, null);

    JFrame frameSeries = chartSeriesCholera.createFrame(this);
    frameSeries.pack();
    c.registerFrame(frameSeries);

    // newly cholera
    chartSeriesCholeraNewly = new sim.util.media.chart.TimeSeriesChartGenerator();
    chartSeriesCholeraNewly.createFrame();
    chartSeriesCholeraNewly.setSize(dm);
    chartSeriesCholeraNewly.setTitle("Health Status - Newly infected");
    chartSeriesCholeraNewly.setRangeAxisLabel("Number of People");
    chartSeriesCholeraNewly.setDomainAxisLabel("Minutes");
    chartSeriesCholeraNewly.setMaximumSize(dm);
    chartSeriesCholeraNewly.setMinimumSize(dmn);
    chartSeriesCholeraNewly.addSeries(((YellowFever) this.state).totalsusceptibleSeriesNewly, null);
    chartSeriesCholeraNewly.addSeries(((YellowFever) this.state).totalExposedSeriesNewly, null);
    chartSeriesCholeraNewly.addSeries(((YellowFever) this.state).totalInfectedSeriesNewly, null);
    chartSeriesCholeraNewly.addSeries(((YellowFever) this.state).totalRecoveredSeriesNewly, null);

    JFrame frameSeriesNewly = chartSeriesCholeraNewly.createFrame(this);
    frameSeriesNewly.pack();
    c.registerFrame(frameSeriesNewly);

    // population dynamics
    chartSeriesPopulation = new sim.util.media.chart.TimeSeriesChartGenerator();
    chartSeriesPopulation.resize(100, 50);
    chartSeriesPopulation.setTitle("Refugee Population Dynamics");
    chartSeriesPopulation.setRangeAxisLabel(" Number of Refugees");
    chartSeriesPopulation.setDomainAxisLabel("Minutes");
    chartSeriesPopulation.addSeries(((YellowFever) this.state).totalTotalPopSeries, null);
    chartSeriesPopulation.addSeries(((YellowFever) this.state).totalDeathSeries, null);

    JFrame frameSeriesPop = chartSeriesPopulation.createFrame(this);

    // frameSeriesPop.setSize(dmn)
    frameSeries.pack();
    c.registerFrame(frameSeriesPop);
    // time

    StandardDialFrame dialFrame = new StandardDialFrame();
    DialBackground ddb = new DialBackground(Color.white);
    dialFrame.setBackgroundPaint(Color.lightGray);
    dialFrame.setForegroundPaint(Color.darkGray);

    DialPlot plot = new DialPlot();
    plot.setView(0.0, 0.0, 1.0, 1.0);
    plot.setBackground(ddb);
    plot.setDialFrame(dialFrame);

    plot.setDataset(0, ((YellowFever) this.state).hourDialer);
    plot.setDataset(1, ((YellowFever) this.state).dayDialer);

    DialTextAnnotation annotation1 = new DialTextAnnotation("Day");
    annotation1.setFont(new Font("Dialog", Font.BOLD, 14));
    annotation1.setRadius(0.1);
    plot.addLayer(annotation1);

    DialValueIndicator dvi2 = new DialValueIndicator(1);
    dvi2.setFont(new Font("Dialog", Font.PLAIN, 22));
    dvi2.setOutlinePaint(Color.red);
    dvi2.setRadius(0.3);
    plot.addLayer(dvi2);

    // TODO: Adicionar dia da semana
    DialTextAnnotation annotation2 = new DialTextAnnotation("Day");
    annotation2.setFont(new Font("Dialog", Font.BOLD, 18));
    annotation2.setRadius(0.4);
    plot.addLayer(annotation2);

    StandardDialScale scale = new StandardDialScale(0.0, 23.99, 90, -360, 1.0, 59);
    scale.setTickRadius(0.9);
    scale.setTickLabelOffset(0.15);
    scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 12));
    plot.addScale(0, scale);
    scale.setMajorTickPaint(Color.black);
    scale.setMinorTickPaint(Color.lightGray);

    DialPointer needle = new DialPointer.Pointer(0);
    plot.addPointer(needle);

    DialCap cap = new DialCap();
    cap.setRadius(0.10);
    plot.setCap(cap);

    JFreeChart chart1 = new JFreeChart(plot);
    ChartFrame timeframe = new ChartFrame("Time Chart", chart1);
    timeframe.setVisible(false);
    timeframe.setSize(200, 100);
    timeframe.pack();
    c.registerFrame(timeframe);

    Dimension dl = new Dimension(300, 700);
    Legend legend = new Legend();
    legend.setSize(dl);

    JFrame legendframe = new JFrame();
    legendframe.setVisible(false);
    legendframe.setPreferredSize(dl);
    legendframe.setSize(300, 700);

    legendframe.setBackground(Color.white);
    legendframe.setTitle("Legend");
    legendframe.getContentPane().add(legend);
    legendframe.pack();
    c.registerFrame(legendframe);
  }

  public Inspector getInspector() {
    super.getInspector();
    TabbedInspector i = new TabbedInspector();

    i.addInspector(new SimpleInspector(((YellowFever) state).getParams().getGlobal(), this), "Paramters");
    return i;
  }

  public void quit() {
    super.quit();
    if (displayFrame != null) {
      displayFrame.dispose();
    }
    displayFrame = null;
    display = null;
    if (displayFrameRainfall != null) {
      displayFrameRainfall.dispose();
    }
    displayFrameRainfall = null;
    displayRainfall = null;
  }

  public static String getName() {
    return "Yellow Fever";
  }

  public Object getSimulationInspectedObject() {
    return state;
  } // non-volatile

}